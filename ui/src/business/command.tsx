export interface CommandRegistryDto extends Record<string, ActionDescriptorDto[]> {
}

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