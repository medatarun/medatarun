import type {
  ActionDescriptorDto,
  ActionDescriptorSemanticsDto,
  ActionDescriptorSemanticsSubjectDto,
  ActionDescriptorSemanticsSubjectReferencingParamDto,
  ActionParamDescriptorDto,
} from "./action-registry-dto.ts";
import type { ActionKey } from "./action-registry-dictionnary.ts";

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
