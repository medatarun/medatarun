import {
  Caption1,
  makeStyles,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Text,
  tokens,
} from "@fluentui/react-components";
import { RelationshipDescription } from "./RelationshipDescription.tsx";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import type { RelationshipDto } from "@/business/model";
import { useModelContext } from "./ModelContext.tsx";
import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import type { ActionCtx } from "@/components/business/actions";
import { Key } from "@/components/core/Key.tsx";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";

const useStyles = makeStyles({
  titleCell: {
    paddingBottom: tokens.spacingVerticalM,
    verticalAlign: "baseline",
    wordBreak: "break-all",
  },
  descriptionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    "& p": {
      marginTop: 0,
    },
  },
  actionCell: {
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    width: "3em",
    verticalAlign: "baseline",
    textAlign: "right",
  },
});

export function RelationshipsTable({
  relationships,
  onClick,
  actionCtxRelationship,
}: {
  relationships: RelationshipDto[];
  onClick: (relationshipId: string) => void;
  actionCtxRelationship: (r: RelationshipDto) => ActionCtx;
}) {
  const { t } = useAppI18n();
  const model = useModelContext();
  const actionRegistry = useActionRegistry();
  const itemActions = actionRegistry.findActions(
    ActionUILocations.relationship,
  );
  const { isDetailLevelTech } = useDetailLevelContext();
  const styles = useStyles();
  return (
    <div>
      {relationships.length == 0 ? (
        <p style={{ paddingTop: tokens.spacingVerticalM }}>
          <Text italic>{t("relationshipsTable_empty")}</Text>
        </p>
      ) : null}
      <div>
        <Table>
          <TableBody>
            {relationships.map((r) => (
              <TableRow key={r.id}>
                <TableCell
                  className={styles.titleCell}
                  onClick={() => onClick(r.id)}
                >
                  <div>{r.name ?? r.key ?? r.id}</div>
                  {r.name && isDetailLevelTech && (
                    <div>
                      <Key value={r.key} />
                    </div>
                  )}
                  {
                    <Caption1>
                      <RelationshipDescription rel={r} />
                    </Caption1>
                  }
                </TableCell>
                <TableCell className={styles.actionCell}>
                  <ActionMenuButton
                    itemActions={itemActions}
                    actionCtx={actionCtxRelationship(r)}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
