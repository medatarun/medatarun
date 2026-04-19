import type { ModelDto, TypeDto } from "./model.dto.ts";
import { includes } from "lodash-es";

export interface TypeOption {
  code: string;
  label: string;
}

export interface EntityOption {
  code: string;
  label: string;
}

export interface EntityAttributeOption {
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

  findEntityAttributeNameOrKey(
    entityId: string,
    attributeId: string,
  ): string | null {
    const e = this.dto.entities
      .find((it) => it.id === entityId)
      ?.attributes?.find((it) => it.id == attributeId);
    return e?.name ?? e?.key ?? null;
  }

  findEntityKey(id: string): string | null {
    const e = this.dto.entities.find((it) => it.id === id);
    return e?.key ?? null;
  }

  get nameOrKey() {
    return this.dto.name ?? this.dto.key;
  }

  get id() {
    return this.dto.id;
  }

  get name() {
    return this.dto.name;
  }

  get key() {
    return this.dto.key;
  }

  get description() {
    return this.dto.description;
  }

  get authority() {
    return this.dto.authority;
  }

  get version() {
    return this.dto.version;
  }

  get documentationHome() {
    return this.dto.documentationHome;
  }

  get tags() {
    return this.dto.tags;
  }

  hasTag(tagId: string) {
    return this.dto.tags.includes(tagId);
  }

  findTagsToDelete(nextTagIds: string[]) {
    return this.dto.tags.filter((tagId) => !nextTagIds.includes(tagId));
  }

  findTagsToAdd(nextTagIds: string[]) {
    return nextTagIds.filter((tagId) => !this.hasTag(tagId));
  }

  get origin() {
    return this.dto.origin;
  }

  get entities() {
    return this.dto.entities;
  }

  get hasEntities() {
    return this.dto.entities.length > 0;
  }

  get relationships() {
    return this.dto.relationships;
  }

  get hasRelationships() {
    return this.dto.relationships.length > 0;
  }

  get types() {
    return this.dto.types;
  }

  get hasTypes() {
    return this.dto.types.length > 0;
  }

  get hasTags() {
    return this.dto.tags.length > 0;
  }

  static authorityEmoji(authority: ModelDto["authority"] | null | undefined) {
    return authority === "canonical" ? "🟩" : "🟦";
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

  /**
   * Returns entity options sorted by their display label to keep combobox choices stable.
   */
  findEntityOptions(): EntityOption[] {
    return [...this.dto.entities]
      .sort((left, right) => {
        const leftLabel = left.name ?? left.key ?? left.id;
        const rightLabel = right.name ?? right.key ?? right.id;
        return leftLabel.localeCompare(rightLabel);
      })
      .map((entity) => ({
        code: entity.id,
        label: entity.name ?? entity.key ?? entity.id,
      }));
  }

  /**
   * Returns attributes for one entity sorted by their display label.
   */
  findEntityAttributeOptions(entityId: string): EntityAttributeOption[] {
    const entity = this.dto.entities.find((it) => it.id === entityId);
    if (!entity) return [];
    return [...entity.attributes]
      .sort((left, right) => {
        const leftLabel = left.name ?? left.key ?? left.id;
        const rightLabel = right.name ?? right.key ?? right.id;
        return leftLabel.localeCompare(rightLabel);
      })
      .map((attribute) => ({
        code: attribute.id,
        label: attribute.name ?? attribute.key ?? attribute.id,
      }));
  }

  /**
   * Returns true if the given attribute id is part of an entity primary key
   */
  isEntityAttributePK(entityId: string, attributeId: string): boolean {
    return (
      this.dto.entityPrimaryKeys.find(
        (it) =>
          it.entityId === entityId && includes(it.participants, attributeId),
      ) !== undefined
    );
  }
  findEntityPKAttributes(entityId: string): string[] {
    return (
      this.dto.entityPrimaryKeys.find((it) => it.entityId === entityId)
        ?.participants ?? []
    );
  }
}
