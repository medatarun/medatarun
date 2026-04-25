import type {
  ActionDescriptorDto,
  ActionDescriptorSemanticsDto,
  ActionDescriptorSemanticsSubjectDto,
  ActionDescriptorSemanticsSubjectReferencingParamDto,
  ActionParamDescriptorDto,
  ActionRegistryDto,
} from "./action_registry.dto.ts";
import { throwError } from "@seij/common-types";
import type { ActionKey } from "@/business/action_registry/actionRegistry.dictionnary.ts";
import { isNil } from "lodash-es";
import { actionRegistryStatic } from "@/business/action_registry/action_registry.static.ts";

export class ActionDescriptor {
  public actionGroupKey: string;
  public key: ActionKey;
  public description: string | null;
  public parameters: ActionDescriptorParam[];
  public title: string;
  public path: string;
  public securityRule: string;
  public semantics: ActionDescriptorSemantics;

  private dto: ActionDescriptorDto;

  constructor(dto: ActionDescriptorDto) {
    this.dto = dto;
    this.actionGroupKey = dto.groupKey;
    this.key = dto.actionKey as ActionKey;
    this.description = dto.description;
    this.parameters = dto.parameters.map((it) => new ActionDescriptorParam(it));
    this.title = dto.title ?? dto.groupKey + "/" + dto.actionKey;
    this.path = dto.groupKey + "/" + dto.actionKey;
    this.securityRule = dto.securityRule;
    this.semantics = new ActionDescriptorSemantics(dto.semantics);
  }
}

export class ActionDescriptorSemantics {
  public intent: string;
  public subjects: ActionDescriptorSemanticsSubject[];
  public returns: string[];

  constructor(dto: ActionDescriptorSemanticsDto) {
    this.intent = dto.intent;
    this.subjects = dto.subjects.map(
      (it) => new ActionDescriptorSemanticsSubject(it),
    );
    this.returns = dto.returns;
  }
}

export class ActionDescriptorSemanticsSubject {
  public type: string;
  public referencingParams: ActionDescriptorSemanticsSubjectReferencingParam[];

  constructor(dto: ActionDescriptorSemanticsSubjectDto) {
    this.type = dto.type;
    this.referencingParams = dto.referencingParams.map(
      (it) => new ActionDescriptorSemanticsSubjectReferencingParam(it),
    );
  }
}

export class ActionDescriptorSemanticsSubjectReferencingParam {
  public name: string;
  public kind: string;

  constructor(dto: ActionDescriptorSemanticsSubjectReferencingParamDto) {
    this.name = dto.name;
    this.kind = dto.kind;
  }
}

export class ActionDescriptorParam {
  public name: string;
  public type: string;
  public optional: boolean;
  public title: string | null;
  public description: string | null;
  public order: number;

  constructor(dto: ActionParamDescriptorDto) {
    this.name = dto.name;
    this.type = dto.type;
    this.order = dto.order;
    this.optional = dto.optional;
    this.title = dto.title;
    this.description = dto.description;
  }
}

export class ActionRegistry {
  public readonly actionGroupKeys: string[];

  private readonly dto: ActionRegistryDto;
  private readonly actionDescriptors: ActionDescriptor[];

  public constructor(dto: ActionRegistryDto) {
    this.dto = dto;
    this.actionGroupKeys = [
      ...new Set(this.dto.items.map((it) => it.groupKey)),
    ];
    this.actionDescriptors = dto.items.map((it) => new ActionDescriptor(it));
  }

  public findActionDtoListByResource(
    groupKey: string | undefined | null,
  ): ActionDescriptor[] {
    if (!groupKey) return [];
    return this.actionDescriptors.filter(
      (it) => it.actionGroupKey === groupKey,
    );
  }

  public findActionOptional(
    actionGroupKey: string | undefined | null,
    actionKey: string | undefined | null,
  ): ActionDescriptor | undefined {
    if (!actionGroupKey) return undefined;
    if (!actionKey) return undefined;
    return this.actionDescriptors.find(
      (it) => actionGroupKey === it.actionGroupKey && actionKey == it.key,
    );
  }

  public findActionByActionKey(
    actionKey: string | undefined | null,
  ): ActionDescriptor | undefined {
    if (!actionKey) return undefined;
    return this.actionDescriptors.find((it) => actionKey == it.key);
  }

  public findActionByGroupKeyAndActionKey(
    actionGroupKey: string,
    actionKey: string,
  ): ActionDescriptor {
    return (
      this.findActionOptional(actionGroupKey, actionKey) ??
      throwError(`Unknown action ${actionGroupKey}/${actionKey}`)
    );
  }

  public findFirstActionKey(resource: string): string | undefined {
    return this.actionDescriptors.find((it) => it.actionGroupKey === resource)
      ?.key;
  }

  public findFirstGroupKey() {
    return this.actionGroupKeys.length == 0
      ? undefined
      : this.actionGroupKeys[0];
  }

  public existsGroup(resource: string) {
    return this.actionGroupKeys.some((it) => it === resource);
  }

  public existsAction(resource: string, action: string) {
    return this.findActionOptional(resource, action) !== undefined;
  }

  public createPayloadTemplate(
    resource: string | undefined | null,
    actionName: string | undefined | null,
  ): string {
    const action = this.findActionOptional(resource, actionName);
    if (!action) return "{}";
    return buildPayloadTemplate(action);
  }

  /**
   * Returns the list of action descriptors you want to display.
   * @param actionKeys keys can contain undefined or null so you can
   * conditionnaly include
   *
   * ```
   * findActionDescriptors("a", canNotAccedd?undefined:"b", "c");
   * ```
   */
  public findActionDescriptors(
    actionKeys: (ActionKey | null | undefined)[],
  ): ActionDescriptor[] {
    return actionKeys
      .filter((it) => !isNil(it))
      .map((it) => this.findActionByActionKey(it))
      .filter((it) => !isNil(it));
  }

  public isNotEmpty(): boolean {
    return this.actionDescriptors.length > 0;
  }
}

function buildPayloadTemplate(action: ActionDescriptor): string {
  const payload: Record<string, string> = {};
  action.parameters.forEach((param) => {
    payload[param.name] = "";
  });
  return JSON.stringify(payload, null, 2);
}

export const ActionRegistryInstance: ActionRegistry = new ActionRegistry(
  actionRegistryStatic,
);
