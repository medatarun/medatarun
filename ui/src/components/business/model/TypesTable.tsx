import type { TypeDto } from "@/business/model";
import {
  Caption1,
  Card,
  CardHeader,
  Text,
  tokens,
} from "@fluentui/react-components";
import { useModelContext } from "./ModelContext.tsx";
import { useDetailLevelContext } from "@/components/business/detail-level";
import { useAppI18n } from "@/services/appI18n.tsx";
import { Key } from "@/components/core/Key.tsx";
import { MarkdownSummary } from "@/components/core/MarkdownSummary.tsx";

export function TypesTable({
  types,
  onClick,
}: {
  types: TypeDto[];
  onClick: (typeId: string) => void;
}) {
  const { t } = useAppI18n();
  const model = useModelContext();
  const { isDetailLevelTech } = useDetailLevelContext();

  return (
    <div>
      {types.length == 0 ? (
        <p style={{ paddingTop: tokens.spacingVerticalL }}>
          <Text italic>{t("typesTable_empty")}</Text>
        </p>
      ) : null}
      <div style={{ paddingTop: tokens.spacingVerticalM }}>
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(4, 1fr)",
            columnGap: tokens.spacingHorizontalM,
            rowGap: tokens.spacingVerticalM,
          }}
        >
          {types.map((type) => {
            return (
              <Card key={type.id} onClick={() => onClick(type.id)}>
                <CardHeader
                  style={{ height: "2em" }}
                  header={
                    <div>
                      <div>{model.findTypeNameOrKey(type.id)}</div>
                      <div>
                        {type.name && isDetailLevelTech && (
                          <Key value={type.key} />
                        )}
                      </div>
                    </div>
                  }
                ></CardHeader>
                <Caption1>
                  <MarkdownSummary value={type.description} maxChars={100} />
                </Caption1>
              </Card>
            );
          })}
        </div>
      </div>
    </div>
  );
}
