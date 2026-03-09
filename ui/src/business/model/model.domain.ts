import type { ModelDto, TypeDto } from "./model.dto.ts";

export interface TypeOption {
  code: string;
  label: string;
}

export class Model {
  public dto: ModelDto;

  constructor(dto: ModelDto) {
    this.dto = dto;
  }

  findEntityNameOrKey(id: string): string | null {
    const e = this.dto.entities.find((it) => it.id === id);
    return e?.name ?? e?.key ?? null;
  }

  findEntityKey(id: string): string | null {
    const e = this.dto.entities.find((it) => it.id === id);
    return e?.key ?? null;
  }

  get nameOrKey() {
    return this.dto.name ?? this.dto.key;
  }

  static authorityEmoji(authority: ModelDto["authority"] | null | undefined) {
    return authority === "canonical" ? "🟩" : "🟦";
  }

  get authorityEmoji() {
    return Model.authorityEmoji(this.dto.authority);
  }

  get nameOrKeyWithAuthorityEmoji() {
    return `${this.authorityEmoji} ${this.nameOrKey}`;
  }

  get id() {
    return this.dto.id;
  }

  findTypeNameOrKey(typeId: string) {
    const type = this.dto.types.find((it) => it.id === typeId);
    return type?.name ?? type?.key ?? type?.id;
  }

  findTypeKey(typeId: string) {
    const type = this.dto.types.find((it) => it.id === typeId);
    return type?.key;
  }

  findEntityDto(entityId: string) {
    return this.dto.entities.find((it) => it.id === entityId);
  }

  findEntityAttributeDto(entityId: string, attributeId: string) {
    return this.dto.entities
      .find((it) => it.id === entityId)
      ?.attributes?.find((it) => it.id === attributeId);
  }

  findRelationshipDto(relationshipId: string) {
    return this.dto.relationships.find((it) => it.id === relationshipId);
  }

  findRelationshipNameOrKey(id: string): string | null {
    const e = this.dto.relationships.find((it) => it.id === id);
    return e?.name ?? e?.key ?? null;
  }

  findRelationshipAttributeDto(relationshipId: string, attributeId: string) {
    return this.dto.relationships
      .find((it) => it.id === relationshipId)
      ?.attributes?.find((it) => it.id === attributeId);
  }

  findTypeDto(typeId: string): TypeDto | undefined {
    return this.dto.types.find((it) => it.id === typeId);
  }

  /**
   * Returns type options sorted by their display label to keep combobox choices stable.
   */
  findTypeOptions(): TypeOption[] {
    return [...this.dto.types]
      .sort((left, right) => {
        const leftLabel = left.name ?? left.key ?? left.id;
        const rightLabel = right.name ?? right.key ?? right.id;
        return leftLabel.localeCompare(rightLabel);
      })
      .map((type) => ({
        code: type.id,
        label: type.name ?? type.key ?? type.id,
      }));
  }
}
