import {InteractionTag, InteractionTagPrimary, makeStyles, TagGroup, tokens} from "@fluentui/react-components";
import {type TagScopeRef, useTags} from "@/business";


const useStyles = makeStyles(
  {
    wrappingTagGroup: {
      flexWrap: "wrap", display: "flex", rowGap: tokens.spacingVerticalS
    }
  }
)

export function Tag({label}: { label: string }) {
  return <InteractionTag>
    <InteractionTagPrimary>{label}</InteractionTagPrimary>
  </InteractionTag>
}

export function modelTagScope(modelId: string): TagScopeRef {
  return {type: "model", id: modelId}
}

export function Tags({tags, scope}: { tags: string[], scope: TagScopeRef }) {
  const styles = useStyles()
  const {tags: tagRegistry} = useTags(scope)

  return <TagGroup className={styles.wrappingTagGroup}>
    {tags.map((it, i) => <Tag key={i} label={tagRegistry.formatLabel(it)}/>)}
  </TagGroup>
}
