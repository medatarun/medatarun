import {
  InteractionTag,
  InteractionTagPrimary,
  makeStyles,
  TagGroup,
  tokens,
} from "@fluentui/react-components";
import { type TagScopeRef } from "@/business/tag";
import { useTags } from "@/components/business/tag";
import { TagRegular } from "@fluentui/react-icons";

const useStyles = makeStyles({
  wrappingTagGroup: {
    flexWrap: "wrap",
    display: "flex",
    rowGap: tokens.spacingVerticalS,
    verticalAlign: "middle",
    alignItems: "baseline",
    alignContent: "baseline",
  },
});

export function Tag({ label }: { label: string }) {
  return (
    <InteractionTag>
      <InteractionTagPrimary icon={<TagRegular />}>
        {label}
      </InteractionTagPrimary>
    </InteractionTag>
  );
}

export function modelTagScope(modelId: string): TagScopeRef {
  return { type: "model", id: modelId };
}

export function Tags({ tags, scope }: { tags: string[]; scope: TagScopeRef }) {
  const styles = useStyles();
  const { tags: tagRegistry } = useTags(scope);

  return (
    <TagGroup
      className={styles.wrappingTagGroup}
      appearance={"outline"}
      size={"small"}
    >
      {tags.map((it, i) => (
        <Tag key={i} label={tagRegistry.formatLabel(it)} />
      ))}
    </TagGroup>
  );
}
export function TagsCondensed({
  tags,
  scope,
}: {
  tags: string[];
  scope: TagScopeRef;
}) {
  const styles = useStyles();
  const { tags: tagRegistry } = useTags(scope);

  return (
    <TagGroup
      className={styles.wrappingTagGroup}
      size={"extra-small"}
      appearance={"outline"}
    >
      {tags.map((it, i) => (
        <Tag key={i} label={tagRegistry.formatLabel(it)} />
      ))}
    </TagGroup>
  );
}
