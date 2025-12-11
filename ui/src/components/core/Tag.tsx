import {InteractionTag, InteractionTagPrimary, TagGroup} from "@fluentui/react-components";


export function Tag({label}:{label: string}) {
  return <InteractionTag>
    <InteractionTagPrimary>{label}</InteractionTagPrimary>
  </InteractionTag>
}

export function Tags({tags}:{tags: string[]}) {
  return <TagGroup>{tags.map((it,i) => <Tag key={i} label={it} />)}</TagGroup>
}