import { Card, CardHeader, Text } from "@fluentui/react-components";
import { type EntityDto } from "@/business/model";
import { modelTagScope, TagsCondensed } from "@/components/core/Tag.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { useModelContext } from "./ModelContext.tsx";
import { Key } from "@/components/core/Key.tsx";
import { MarkdownSummary } from "@/components/core/MarkdownSummary.tsx";

export function EntityCard({
  entity,
  onClick,
}: {
  entity: EntityDto;
  onClick: (id: string) => void;
}) {
  const model = useModelContext();
  const { isDetailLevelTech } = useDetailLevelContext();
  return (
    <Card onClick={() => onClick(entity.id)}>
      <CardHeader
        style={{ height: "2em" }}
        header={
          <Text weight="semibold">
            <div>{entity.name ?? <Key value={entity.key ?? entity.id} />}</div>
            <div>
              {entity.name && isDetailLevelTech && <Key value={entity.key} />}
            </div>
          </Text>
        }
      ></CardHeader>
      <div style={{ minHeight: "4em", maxHeight: "4em", overflow: "hidden" }}>
        {entity.description && (
          <MarkdownSummary value={entity.description} maxChars={150} />
        )}
      </div>
      <TagsCondensed tags={entity.tags} scope={modelTagScope(model.id)} />
    </Card>
  );
}
