import type {TagDto, TagGroupDto, TagScopeRef} from "./tag.dto.ts";

export class TagGroup {
  readonly id: string
  readonly key: string
  readonly name: string | null
  readonly description: string | null

  constructor(dto: TagGroupDto) {
    this.id = dto.id
    this.key = dto.key
    this.name = dto.name
    this.description = dto.description
  }

  get label(): string {
    return this.name ?? this.key
  }
}

export class Tag {
  readonly id: string
  readonly key: string
  readonly groupId: string | null
  readonly scope: TagScopeRef
  readonly name: string | null
  readonly description: string | null
  readonly groupKey: string | null
  readonly groupName: string | null

  constructor(dto: TagDto, tagGroup: TagGroup | undefined) {
    this.id = dto.id
    this.key = dto.key
    this.groupId = dto.groupId
    this.scope = dto.tagScopeRef
    this.name = dto.name
    this.description = dto.description
    this.groupKey = tagGroup?.key ?? null
    this.groupName = tagGroup?.name ?? null
  }

  get label(): string {
    return this.name ?? this.key
  }

  get isGlobal(): boolean {
    return this.scope.type === "global"
  }

  get isLocal(): boolean {
    return !this.isGlobal
  }

  get isManaged(): boolean {
    return this.isGlobal
  }

  get isFree(): boolean {
    return this.isLocal
  }


  get scopeLabel(): string {
    if (this.isGlobal) {
      return "Global"
    }
    return `${this.scope.type} / ${this.scope.id}`
  }

  get groupLabel(): string | null {
    if (!this.groupId) {
      return null
    }
    return this.groupName ?? this.groupKey
  }
}

/**
 * Provides a single place to resolve TagId values into UI-friendly data.
 * The UI stores ids on business objects, while screens need stable lookup and formatting helpers.
 */
export class Tags {
  private readonly tagsById: Map<string, Tag>
  private readonly groupsById: Map<string, TagGroup>
  private readonly tags: Tag[]

  constructor(tags: TagDto[], groups: TagGroupDto[]) {
    this.groupsById = new Map()
    for (const groupDto of groups) {
      const group = new TagGroup(groupDto)
      this.groupsById.set(group.id, group)
    }

    this.tags = tags.map(tag => new Tag(tag, tag.groupId ? this.groupsById.get(tag.groupId) : undefined))
    this.tagsById = new Map()
    for (const tag of this.tags) {
      this.tagsById.set(tag.id, tag)
    }
  }

  all(): Tag[] {
    return this.tags
  }

  listTagGroups(): TagGroup[] {
    return [...this.groupsById.values()]
  }

  findTag(tagId: string): Tag | undefined {
    return this.tagsById.get(tagId)
  }

  findTagGroup(tagGroupId: string): TagGroup | undefined {
    return this.groupsById.get(tagGroupId)
  }

  findTagByScopeAndKey(scope: TagScopeRef, key: string): Tag | undefined {
    return this.tags.find(tag => tag.key === key && this.sameScope(tag.scope, scope))
  }

  findTagsByScope(scope: TagScopeRef, tagGroupId?: string): Tag[] {
    return this.tags.filter(tag => this.sameScope(tag.scope, scope) && (tagGroupId == null || tag.groupId === tagGroupId))
  }

  /**
   * Returns a short placeholder when the tag metadata is missing.
   * The UI stores TagId values, but showing a raw id would create noisy and unstable layouts,
   * especially in cards and tables where a long identifier stretches the content unexpectedly.
   */
  formatLabel(tagId: string): string {
    const tag = this.findTag(tagId)
    if (!tag) {
      return "..."
    }
    if (!tag.groupKey) {
      return tag.key
    }
    return `${tag.groupKey} / ${tag.key}`
  }

  /**
   * Search is based on the technical fields that users already understand today:
   * tag key, optional tag name, managed group key, and optional managed group name.
   */
  search(query: string, excludedTagIds: string[]): Tag[] {
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

  private searchableText(tag: Tag): string {
    const parts = [tag.key]
    if (tag.name) {
      parts.push(tag.name)
    }
    if (tag.groupKey) {
      parts.push(tag.groupKey)
    }
    if (tag.groupName) {
      parts.push(tag.groupName)
    }
    return parts.join(" ").toLocaleLowerCase()
  }

  private sameScope(left: TagScopeRef, right: TagScopeRef): boolean {
    return left.type === right.type && left.id === right.id
  }
}
