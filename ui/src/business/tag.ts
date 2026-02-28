import type {TagGroupListItemDto, TagScopeRef, TagSearchItemDto} from "./action_perform.hooks.ts";

/**
 * Provides a single place to resolve TagId values into UI-friendly data.
 * The UI stores ids on business objects, while screens need stable lookup and formatting helpers.
 */
export class TagList {
  private readonly tagsById: Map<string, TagSearchItemDto>
  private readonly groupKeysById: Map<string, string>
  private readonly groupNamesById: Map<string, string>
  private readonly tags: TagSearchItemDto[]

  constructor(tags: TagSearchItemDto[], groups: TagGroupListItemDto[]) {
    this.tags = tags
    this.tagsById = new Map()
    for (const tag of tags) {
      this.tagsById.set(tag.id, tag)
    }

    this.groupKeysById = new Map()
    this.groupNamesById = new Map()
    for (const group of groups) {
      this.groupKeysById.set(group.id, group.key)
      if (group.name) {
        this.groupNamesById.set(group.id, group.name)
      }
    }
  }

  findById(tagId: string): TagSearchItemDto | undefined {
    return this.tagsById.get(tagId)
  }

  findByScopeAndKey(scope: TagScopeRef, key: string): TagSearchItemDto | undefined {
    return this.tags.find(tag => tag.key === key && this.sameScope(tag.tagScopeRef, scope))
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

  /**
   * Search is based on the technical fields that users already understand today:
   * tag key, optional tag name, managed group key, and optional managed group name.
   */
  search(query: string, excludedTagIds: string[]): TagSearchItemDto[] {
    const normalizedQuery = query.trim().toLocaleLowerCase()
    const excludedTagIdsSet = new Set(excludedTagIds)

    return this.tags.filter(tag => {
      if (excludedTagIdsSet.has(tag.id)) {
        return false
      }
      if (normalizedQuery === "") {
        return true
      }
      return this.searchableText(tag).includes(normalizedQuery)
    })
  }

  private searchableText(tag: TagSearchItemDto): string {
    const parts = [tag.key]
    if (tag.name) {
      parts.push(tag.name)
    }
    if (tag.groupId) {
      const groupKey = this.groupKeysById.get(tag.groupId)
      if (groupKey) {
        parts.push(groupKey)
      }
      const groupName = this.groupNamesById.get(tag.groupId)
      if (groupName) {
        parts.push(groupName)
      }
    }
    return parts.join(" ").toLocaleLowerCase()
  }

  private sameScope(left: TagScopeRef, right: TagScopeRef): boolean {
    return left.type === right.type && left.id === right.id
  }
}
