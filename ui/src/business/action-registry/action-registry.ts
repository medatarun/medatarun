import type { ActionRegistryDto } from "./action-registry-dto.ts";
import { throwError } from "@seij/common-types";
import type { ActionKey } from "./action-registry-dictionnary.ts";
import { isNil } from "lodash-es";
import { ActionDescriptor } from "./action-descriptor.ts";

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

  public findActionDescriptorsByGroupKey(
    groupKey: string | undefined | null,
  ): ActionDescriptor[] {
    if (!groupKey) return [];
    return this.actionDescriptors.filter((it) =>
      it.actionRef.startsWith(groupKey + "/"),
    );
  }

  public findActionDescriptorOptional(
    actionRef: string | undefined | null,
  ): ActionDescriptor | undefined {
    if (!actionRef) return undefined;
    return this.actionDescriptors.find((it) => it.actionRef == actionRef);
  }

  public findActionDescriptor(actionRef: string): ActionDescriptor {
    return (
      this.findActionDescriptorOptional(actionRef) ??
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
    return this.findActionDescriptorOptional(actionRef) !== undefined;
  }

  public createPayloadTemplate(actionRef: string | undefined | null): string {
    const action = this.findActionDescriptorOptional(actionRef);
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
      .map((it) => this.findActionDescriptorOptional(it))
      .filter((it) => !isNil(it));
  }
}

function buildPayloadTemplate(action: ActionDescriptor): string {
  const payload: Record<string, string> = {};
  action.parameters.forEach((param) => {
    payload[param.name] = "";
  });
  return JSON.stringify(payload, null, 2);
}
