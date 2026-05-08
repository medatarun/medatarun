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
  public actionRef: ActionKey;
  public description: string | null;
  public parameters: ActionDescriptorParam[];
  public title: string;
  public path: string;
  public securityRule: string;
  public semantics: ActionDescriptorSemantics;

  private dto: ActionDescriptorDto;

  constructor(dto: ActionDescriptorDto) {
    this.dto = dto;
    this.actionRef = dto.actionRef as ActionKey;
    this.description = dto.description;
    this.parameters = dto.parameters.map((it) => new ActionDescriptorParam(it));
    this.title = dto.title ?? dto.actionRef;
    this.path = dto.actionRef;
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
    this.actionDescriptors = dto.items.map((it) => new ActionDescriptor(it));
    this.actionGroupKeys = [
      ...new Set(dto.items.map((it) => it.actionRef.split("/")[0])),
    ];
  }

  public findActionDtoListByResource(
    groupKey: string | undefined | null,
  ): ActionDescriptor[] {
    if (!groupKey) return [];
    return this.actionDescriptors.filter((it) =>
      it.actionRef.startsWith(groupKey + "/"),
    );
  }

  public findActionOptional(
    actionRef: string | undefined | null,
  ): ActionDescriptor | undefined {
    if (!actionRef) return undefined;
    return this.actionDescriptors.find((it) => it.actionRef == actionRef);
  }

  public findActionByActionKey(
    actionKey: string | undefined | null,
  ): ActionDescriptor | undefined {
    if (!actionKey) return undefined;
    return this.actionDescriptors.find((it) => actionKey == it.actionRef);
  }

  public findActionByGroupKeyAndActionKey(actionRef: string): ActionDescriptor {
    return (
      this.findActionOptional(actionRef) ??
      throwError(`Unknown action ${actionRef}`)
    );
  }

  public findFirstActionRefOfGroup(groupKey: string): string | undefined {
    return this.actionDescriptors.find((it) =>
      it.actionRef.startsWith(groupKey + "/"),
    )?.actionRef;
  }

  public findFirstGroupKey() {
    return this.actionGroupKeys.length == 0
      ? undefined
      : this.actionGroupKeys[0];
  }

  public existsGroup(actionGroupKey: string) {
    return this.actionGroupKeys.some((it) => it === actionGroupKey);
  }

  public existsAction(actionRef: string) {
    return this.findActionOptional(actionRef) !== undefined;
  }

  public createPayloadTemplate(actionRef: string | undefined | null): string {
    const action = this.findActionOptional(actionRef);
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
