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
  semantics: ActionDescriptorSemanticsDto;
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

export interface ActionDescriptorSemanticsDto {
  intent: string;
  subjects: ActionDescriptorSemanticsSubjectDto[];
  returns: string[];
}

export interface ActionDescriptorSemanticsSubjectDto {
  type: string;
  referencingParams: ActionDescriptorSemanticsSubjectReferencingParamDto[];
}

export interface ActionDescriptorSemanticsSubjectReferencingParamDto {
  name: string;
  kind: string;
}
