export type ActionRegistryDto = ActionDescriptorDto[]

export interface ActionDescriptorDto {
  actionKey: string,
  groupKey: string,
  title: string | null,
  description: string | null,
  parameters: ActionParamDescriptorDto[],
  uiLocation: string
}

export interface ActionParamDescriptorDto {
  name: string
  type: string
  optional: boolean
  title: string | null
  description: string | null,
  order: number
}
