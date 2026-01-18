import {InteractionTag, InteractionTagPrimary, makeStyles, TagGroup, tokens} from "@fluentui/react-components";


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

export function Tags({tags}: { tags: string[] }) {
  const styles = useStyles()
  return <TagGroup className={styles.wrappingTagGroup}>{tags.map((it, i) => <Tag key={i} label={it}/>)}</TagGroup>
}