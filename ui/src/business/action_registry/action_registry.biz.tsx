import type {ActionDescriptorDto, ActionParamDescriptorDto, ActionRegistryDto} from "./action_registry.dto.ts";
import type {ActionUILocation} from "./action_registry.uilocations.ts";
import {throwError} from "@seij/common-types";

export class ActionDescriptor {
  public actionGroupKey: string
  public key: string;
  public description: string | null
  public parameters: ActionDescriptorParam[];
  public title: string;
  public path: string

  private dto: ActionDescriptorDto;

  constructor(dto: ActionDescriptorDto) {
    this.dto = dto
    this.actionGroupKey = dto.groupKey
    this.key = dto.actionKey
    this.description = dto.description
    this.parameters = dto.parameters.map(it => new ActionDescriptorParam(it))
    this.title = dto.title ?? dto.groupKey + "/" + dto.actionKey
    this.path = dto.groupKey + "/" + dto.actionKey
  }


  matchesLocation(location: string): boolean {
    return this.dto.uiLocations.includes(location)
  }

}

export class ActionDescriptorParam {
  public name: string;
  public type: string;
  public optional: boolean;
  public title: string|null;
  public description: string|null;
  public order: number;

  constructor(dto: ActionParamDescriptorDto) {
    this.name = dto.name
    this.type = dto.type
    this.order = dto.order
    this.optional = dto.optional
    this.title = dto.title
    this.description = dto.description
  }
}

export class ActionRegistry {
  public readonly actionGroupKeys: string[]

  private readonly dto: ActionRegistryDto;
  private readonly actionDescriptors: ActionDescriptor[]

  public constructor(dto: ActionRegistryDto) {
    this.dto = dto;
    this.actionGroupKeys = [...new Set(this.dto.map(it => it.groupKey))]
    this.actionDescriptors = dto.map(it => new ActionDescriptor(it))
  }

  public findActionDtoListByResource(groupKey: string | undefined | null): ActionDescriptor[] {
    if (!groupKey) return []
    return this.actionDescriptors.filter(it => it.actionGroupKey === groupKey)
  }

  public findActionOptional(actionGroupKey: string | undefined | null, actionKey: string | undefined | null): ActionDescriptor | undefined {
    if (!actionGroupKey) return undefined
    if (!actionKey) return undefined
    return this.actionDescriptors.find(it => actionGroupKey === it.actionGroupKey && actionKey == it.key)
  }

  public findAction(actionGroupKey: string, actionKey: string) : ActionDescriptor{
    return this.findActionOptional(actionGroupKey, actionKey) ?? throwError(`Unknown action ${actionGroupKey}/${actionKey}`)
  }

  public findFirstActionKey(resource: string): string | undefined {
    return this.actionDescriptors.find(it => it.actionGroupKey === resource)?.key
  }

  public findFirstGroupKey() {
    return this.actionGroupKeys.length == 0 ? undefined : this.actionGroupKeys[0]
  }

  public existsGroup(resource: string) {
    return this.actionGroupKeys.some(it => it === resource)
  }

  public existsAction(resource: string, action: string) {
    return this.findActionOptional(resource, action) !== undefined
  }

  public createPayloadTemplate(resource: string | undefined | null, actionName: string | undefined | null): string {
    const action = this.findActionOptional(resource, actionName)
    if (!action) return "{}"
    return buildPayloadTemplate(action)
  }

  public findActions(location: ActionUILocation): ActionDescriptor[] {
    return this.actionDescriptors.filter(it => it.matchesLocation(location))
  }

}


function buildPayloadTemplate(action: ActionDescriptor): string {
  const payload: Record<string, string> = {}
  action.parameters.forEach(param => {
    payload[param.name] = ""
  })
  return JSON.stringify(payload, null, 2)
}

