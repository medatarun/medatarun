import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import type { TypeDto } from "@/business/model";
import {
  Caption1,
  Card,
  CardHeader,
  makeStyles,
  Text,
  tokens,
} from "@fluentui/react-components";
import { useModelContext } from "./ModelContext.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { createDisplayedSubjectModel } from "./model.actions.ts";
import { useAppI18n } from "@/services/appI18n.tsx";
import { Key } from "@/components/core/Key.tsx";
import { MarkdownSummary } from "@/components/core/MarkdownSummary.tsx";

const useStyles = makeStyles({
  titleCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "20rem",
    verticalAlign: "baseline",
    wordBreak: "break-all",
  },
  flags: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "1em",
    verticalAlign: "baseline",
  },
  descriptionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    verticalAlign: "baseline",
    "& p": {
      marginTop: 0,
    },
    "& p:last-child": {
      marginBottom: 0,
    },
  },
  typeCodeCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    verticalAlign: "baseline",
    width: "10rem",
  },
  actionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "3em",
    verticalAlign: "baseline",
    textAlign: "right",
  },
});

export function TypesTable({
  types,
  onClick,
}: {
  types: TypeDto[];
  onClick: (typeId: string) => void;
}) {
  const { t } = useAppI18n();
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const itemActions = actionRegistry.findActions(ActionUILocations.type);
  const { isDetailLevelTech } = useDetailLevelContext();
  const styles = useStyles();
  const displayedSubject = createDisplayedSubjectModel(model.id);

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
