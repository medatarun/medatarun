export interface ActionRegistryDto {
  readonly items: ActionDescriptorDto[];
}

export interface ActionDescriptorDto {
  readonly id: string;
  readonly actionKey: string;
  readonly groupKey: string;
  readonly title: string | null;
  readonly description: string | null;
  readonly parameters: ActionParamDescriptorDto[];
  readonly uiLocations: string[];
  readonly securityRule: string;
  readonly semantics: ActionDescriptorSemanticsDto;
}

export interface ActionParamDescriptorDto {
  readonly name: string;
  readonly type: string;
  readonly jsonType: string;
  readonly optional: boolean;
  readonly title: string | null;
  readonly description: string | null;
  readonly order: number;
}

export interface ActionDescriptorSemanticsDto {
  readonly intent: string;
  readonly subjects: ActionDescriptorSemanticsSubjectDto[];
  readonly returns: string[];
}

export interface ActionDescriptorSemanticsSubjectDto {
  readonly type: string;
  readonly referencingParams: ActionDescriptorSemanticsSubjectReferencingParamDto[];
}

export interface ActionDescriptorSemanticsSubjectReferencingParamDto {
  readonly name: string;
  readonly kind: string;
}
