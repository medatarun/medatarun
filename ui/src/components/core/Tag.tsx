import {InteractionTag, InteractionTagPrimary, TagGroup} from "@fluentui/react-components";
import {ErrorBox} from "@seij/common-ui";
import {toProblem} from "@seij/common-types";


export function Tag({label}:{label: string}) {
  return <InteractionTag>
    <InteractionTagPrimary>{label}</InteractionTagPrimary>
  </InteractionTag>
}

export function Tags({tags}:{tags: string[]}) {
  if (!tags) return <ErrorBox error={toProblem("Tags are undefined")}></ErrorBox>
  return <TagGroup>{tags.map((it,i) => <Tag key={i} label={it} />)}</TagGroup>
}