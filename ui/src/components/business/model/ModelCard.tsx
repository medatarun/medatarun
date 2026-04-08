import {
  Card,
  CardHeader,
  Tag,
  TagGroup,
  Text,
} from "@fluentui/react-components";
import type { ModelSummaryDto } from "@/business/model";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { EntityIcon, RelationshipIcon, TypeIcon } from "./model.icons.tsx";
import { MarkdownSummary } from "@/components/core/MarkdownSummary.tsx";
import { Key } from "@/components/core/Key.tsx";

export function ModelCard({
  model,
  onClick,
}: {
  model: ModelSummaryDto;
  onClick: (id: string) => void;
}) {
  const { isDetailLevelTech } = useDetailLevelContext();

  return (
    <Card
      style={{ maxWidth: "20em", width: "20em", minWidth: "20em" }}
      key={model.id}
      onClick={() => onClick(model.id)}
    >
      <CardHeader
        style={{ height: "3em", overflow: "hidden", alignItems: "start" }}
        header={
          <div>
            <div>
              <Text weight="semibold" style={{ maxHeight: "3em" }}>
                {model.name ?? model.key ?? model.id}
              </Text>
            </div>
            {isDetailLevelTech && (
              <div>
                <Key value={model.key} />
              </div>
            )}
          </div>
        }
      ></CardHeader>

      <div style={{ minHeight: "4em", maxHeight: "4em", overflow: "hidden" }}>
        {model.description && (
          <MarkdownSummary value={model.description} maxChars={100} />
        )}
        {model.error && <div style={{ color: "red" }}>{model.error}</div>}
      </div>

      <div>
        <TagGroup size="extra-small">
          <Tag size="extra-small" appearance="outline" icon={<EntityIcon />}>
            {model.countEntities}
          </Tag>
          <Tag
            size="extra-small"
            appearance="outline"
            icon={<RelationshipIcon />}
          >
            {model.countRelationships}
          </Tag>
          <Tag size="extra-small" appearance="outline" icon={<TypeIcon />}>
            {model.countTypes}
          </Tag>
        </TagGroup>
      </div>
    </Card>
  );
}
