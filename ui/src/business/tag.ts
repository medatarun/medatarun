import type {TagGroupListItemDto, TagSearchItemDto} from "./action_perform.hooks.ts";

/**
 * Provides a single place to resolve TagId values into UI-friendly data.
 * The UI stores ids on business objects, while screens need stable lookup and formatting helpers.
 */
export class TagList {
  private readonly tagsById: Map<string, TagSearchItemDto>
  private readonly groupKeysById: Map<string, string>

  constructor(tags: TagSearchItemDto[], groups: TagGroupListItemDto[]) {
    this.tagsById = new Map()
    for (const tag of tags) {
      this.tagsById.set(tag.id, tag)
    }

    this.groupKeysById = new Map()
    for (const group of groups) {
      this.groupKeysById.set(group.id, group.key)
    }
  }

  findById(tagId: string): TagSearchItemDto | undefined {
    return this.tagsById.get(tagId)
  }

  /**
   * Returns a short placeholder when the tag metadata is missing.
   * The UI stores TagId values, but showing a raw id would create noisy and unstable layouts,
   * especially in cards and tables where a long identifier stretches the content unexpectedly.
   */
  formatLabel(tagId: string): string {
    const tag = this.findById(tagId)
    if (!tag) {
      return "..."
    }
    if (tag.groupId == null) {
      return tag.key
    }
    const groupKey = this.groupKeysById.get(tag.groupId)
    if (!groupKey) {
      return tag.key
    }
    return `${groupKey} / ${tag.key}`
  }
}
