export type ActionRegistryDto = Record<string, ActionDescriptorDto[]>

export interface ActionDescriptorDto {
  name: string,
  title: string | null,
  description: string | null,
  parameters: ActionParamDescriptorDto[]
}

export interface ActionParamDescriptorDto {
  name: string
  type: string
  optional: boolean
}

type CommandTargetType = "model" | "entity" | "entityAttribute" | "relationship" | "relationshipAttribute" | "none"

export class Command {
  private dto: ActionDescriptorDto;
  public isModelKey: boolean;
  public isEntityKey: boolean;
  public isAttributeKey: boolean;
  public isRelationshipKey: boolean;
  public targetType: CommandTargetType

  constructor(dto: ActionDescriptorDto) {
    this.dto = dto
    this.isModelKey = this.dto.parameters.some(it => it.name === "modelKey")
    this.isEntityKey = this.dto.parameters.some(it => it.name === "entityKey")
    this.isAttributeKey = this.dto.parameters.some(it => it.name === "attributeKey")
    this.isRelationshipKey = this.dto.parameters.some(it => it.name === "relationshipKey")
    this.targetType = this.findTargetType()
  }

  private findTargetType(): CommandTargetType {
    if (this.isAttributeKey && this.isEntityKey && this.isModelKey) return "entityAttribute"
    if (this.isAttributeKey && this.isRelationshipKey && this.isModelKey) return "relationshipAttribute"
    if (this.isEntityKey && this.isModelKey) return "entity"
    if (this.isRelationshipKey && this.isModelKey) return "relationship"
    if (this.isModelKey) return "model"
    return "none"
  }

}

export class ActionRegistry {
  public readonly dto: ActionRegistryDto;
  public readonly actionGroupKeys: string[]

  public constructor(dto: ActionRegistryDto) {
    this.dto = dto;
    this.actionGroupKeys = Object.keys(this.dto)
  }

  public findActionDtoListByResource(resource: string | undefined | null): ActionDescriptorDto[] {
    if (!resource) return []
    return this.dto[resource] ?? []
  }

  public findActionDto(resource: string | undefined | null, actionName: string | undefined | null): ActionDescriptorDto | undefined {
    if (!resource) return undefined
    if (!actionName) return undefined
    return this.dto[resource]?.find(it => actionName == it.name)
  }

  public findFirstActionKey(resource: string) {
    const r = this.dto[resource]
    if (!r) return undefined
    return r.length == 0 ? undefined : r[0].name
  }

  public findFirstGroupKey() {
    return this.actionGroupKeys.length == 0 ? undefined : this.actionGroupKeys[0]
  }

  public existsGroup(resource: string) {
    return this.dto[resource] !== undefined
  }

  public existsAction(resource: string, action: string) {
    return this.findActionDto(resource, action) !== undefined
  }

  public createPayloadTemplate(resource: string | undefined | null, actionName: string | undefined | null): string {
    const action = this.findActionDto(resource, actionName)
    if (!action) return "{}"
    return buildPayloadTemplate(action)
  }
}


function buildPayloadTemplate(action: ActionDescriptorDto): string {
  const payload: Record<string, string> = {}
  action.parameters.forEach(param => {
    payload[param.name] = ""
  })
  return JSON.stringify(payload, null, 2)
}


// ---------------------------------------------------------------------------------------------------------------------
// Backend communications
// ---------------------------------------------------------------------------------------------------------------------

export async function fetchActionDescriptors(): Promise<ActionRegistryDto> {
  return fetch("/api")
    .then(res => res.json())
}

export async function executeAction(actionGroup: string, actionName: string, payload: unknown): Promise<unknown> {
  return fetch("/api/" + actionGroup + "/" + actionName, {method: "POST", body: JSON.stringify(payload)})
    .then(async res => {
      const type = res.headers.get("content-type") || "";
      if (type.includes("application/json")) {
        return res.json();
      }
      const t = await res.text();
      return t;
    })
}