export type ActionRegistryDto = Record<string, ActionDescriptorDto[]>

interface ActionDescriptorDto {
  name: string,
  title: string | null,
  description: string | null,
  parameters: ActionParamDescriptorDto[]
}

interface ActionParamDescriptorDto {
  name: string
  type: string
  optional: boolean
}

type CommandTargetType = "model" | "entity" | "entityAttribute" | "relationship" | "relationshipAttribute" | "none"

export class ActionDescriptor {
  public actionGroupKey: string
  public isModelKey: boolean;
  public isEntityKey: boolean;
  public isAttributeKey: boolean;
  public isRelationshipKey: boolean;
  public targetType: CommandTargetType
  public key: string;
  public description: string | null
  public parameters: ActionDescriptorParam[];

  private dto: ActionDescriptorDto;

  constructor(actionGroupKey: string, dto: ActionDescriptorDto) {
    this.dto = dto
    this.actionGroupKey = actionGroupKey
    this.isModelKey = this.dto.parameters.some(it => it.name === "modelKey")
    this.isEntityKey = this.dto.parameters.some(it => it.name === "entityKey")
    this.isAttributeKey = this.dto.parameters.some(it => it.name === "attributeKey")
    this.isRelationshipKey = this.dto.parameters.some(it => it.name === "relationshipKey")
    this.targetType = this.findTargetType()
    this.key = dto.name
    this.description = dto.description
    this.parameters = dto.parameters.map(it => new ActionDescriptorParam(it))
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

export class ActionDescriptorParam {
  public name: string;
  public type: string;
  public optional: boolean;

  constructor(dto: ActionParamDescriptorDto) {
    this.name = dto.name
    this.type = dto.type
    this.optional = dto.optional
  }
}

export class ActionRegistry {
  public readonly actionGroupKeys: string[]

  private readonly dto: ActionRegistryDto;
  private readonly actionDescriptors: ActionDescriptor[]

  public constructor(dto: ActionRegistryDto) {
    this.dto = dto;
    this.actionGroupKeys = Object.keys(this.dto)
    const desc: ActionDescriptor[] = []
    for (const actionGroupKey of Object.keys(this.dto)) {
      const actionGroup = this.dto[actionGroupKey]
      if (actionGroup) {
        for (const actionDescriptorDto of actionGroup) {
          const a = new ActionDescriptor(actionGroupKey, actionDescriptorDto)
          desc.push(a)
        }
      }
    }
    this.actionDescriptors = desc
  }

  public findActionDtoListByResource(groupKey: string | undefined | null): ActionDescriptor[] {
    if (!groupKey) return []
    return this.actionDescriptors.filter(it => it.actionGroupKey === groupKey)
  }

  public findAction(actionGroupKey: string | undefined | null, actionKey: string | undefined | null): ActionDescriptor | undefined {
    if (!actionGroupKey) return undefined
    if (!actionKey) return undefined
    return this.actionDescriptors.find(it => actionGroupKey === it.actionGroupKey && actionKey == it.key)
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
    return this.findAction(resource, action) !== undefined
  }

  public createPayloadTemplate(resource: string | undefined | null, actionName: string | undefined | null): string {
    const action = this.findAction(resource, actionName)
    if (!action) return "{}"
    return buildPayloadTemplate(action)
  }
}


function buildPayloadTemplate(action: ActionDescriptor): string {
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