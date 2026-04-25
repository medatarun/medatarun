import { Card, CardHeader, Text } from "@fluentui/react-components";
import type { ModelListItemDto } from "@/business/model";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { MarkdownSummary } from "@/components/core/MarkdownSummary.tsx";
import { Key } from "@/components/core/Key.tsx";

export function ModelCard({
  model,
  onClick,
}: {
  model: ModelListItemDto;
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
      </div>
    </Card>
  );
}
