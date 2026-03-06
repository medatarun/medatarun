export interface ActionRegistryDto {
  items: ActionDescriptorDto[];
}

export interface ActionDescriptorDto {
  actionKey: string;
  groupKey: string;
  title: string | null;
  description: string | null;
  parameters: ActionParamDescriptorDto[];
  uiLocations: string[];
  securityRule: string;
}

export interface ActionParamDescriptorDto {
  name: string;
  type: string;
  jsonType: string;
  optional: boolean;
  title: string | null;
  description: string | null;
  order: number;
}
