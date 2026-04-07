import { useEffect, useMemo, useState } from "react";
import { type ActionResp, executeAction } from "@/business/action_runner";
import { ActionRegistry, useActionRegistry } from "@/business/action_registry";
import { ActionOutput } from "@/components/business/actions/ActionOutput.tsx";
import { SecurityRuleBadge } from "@/views/actions/components/SecurityRuleBadge.tsx";
import { Markdown } from "@/components/core/Markdown.tsx";
import { MissingInformation } from "@/components/core/MissingInformation.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import {
  Button,
  Field,
  makeStyles,
  mergeClasses,
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHeaderCell,
  TableRow,
  Textarea,
  tokens,
  Tree,
  TreeItem,
  TreeItemLayout,
  type TreeItemValue,
} from "@fluentui/react-components";
import {
  CheckmarkRegular, CodeBlockRegular,
  CopyRegular,
  DismissRegular,
} from "@fluentui/react-icons";
import { useAppI18n } from "@/services/appI18n.tsx";
import { Problem, type ProblemJson } from "@seij/common-types";
import { ErrorBox } from "@seij/common-ui";
import { sortBy } from "lodash-es";
import  { ViewLayoutHeader, type ViewLayoutHeaderProps } from "@/components/layout/ViewLayoutHeader.tsx";

const useActionTreeStyles = makeStyles({
  root: {
    padding: tokens.spacingVerticalM,
  },
  actionItem: {
    fontWeight: tokens.fontWeightRegular,
  },
  actionItemSelected: {
    fontWeight: tokens.fontWeightSemibold,
    backgroundColor: tokens.colorNeutralBackground1Selected,
    borderRadius: tokens.borderRadiusSmall,
  },
});

const useActionPageStyles = makeStyles({
  root: {
    display: "flex",
    flexDirection: "column",
    height: "100%",
    minHeight: 0,
  },
  splitArea: {
    display: "grid",
    gridTemplateColumns: "minmax(100px, 1fr) minmax(0, 4fr)",
    columnGap: "1em",
    flex: 1,
    minHeight: 0,
  },
  masterPane: {
    minHeight: 0,
    overflowY: "auto",
    borderRight: `${tokens.strokeWidthThin} solid ${tokens.colorNeutralStroke2}`,
    backgroundColor: tokens.colorNeutralBackground1,
  },
  detailPane: {
    minHeight: 0,
    overflowY: "auto",
    paddingLeft: tokens.spacingHorizontalM,
    paddingTop: tokens.spacingVerticalM,
    paddingRight: tokens.spacingHorizontalM,
    paddingBottom: tokens.spacingVerticalM,
  },
});

const useActionLauncherStyles = makeStyles({
  root: {
    display: "flex",
    flexDirection: "column",
    gap: "1em",
    minHeight: 0,
  },
  titleRow: {
    display: "flex",
    alignItems: "flex-start",
    justifyContent: "space-between",
    gap: tokens.spacingHorizontalM,
  },
  titleBlock: {
    minWidth: 0,
  },
  title: {
    fontSize: tokens.fontSizeBase400,
    fontWeight: tokens.fontWeightSemibold,
  },
  subtitle: {
    color: tokens.colorNeutralForeground3,
    fontSize: tokens.fontSizeBase300,
    marginTop: tokens.spacingVerticalXXS,
  },
  securityRule: {
    whiteSpace: "nowrap",
    flexShrink: 0,
  },
  actionDescription: {
    marginBottom: "0.5em",
  },
  markdownCompact: {
    "& p": {
      marginTop: 0,
      marginBottom: 0,
    },
  },
  parametersTableWrapper: {
    overflowX: "auto",
  },
  parametersTable: {
    width: "100%",
    minWidth: "640px",
  },
  parameterCol: {
    width: "clamp(120px, 18vw, 260px)",
  },
  typeCol: {
    width: "clamp(90px, 10vw, 160px)",
    whiteSpace: "nowrap",
  },
  descriptionCol: {
    width: "auto",
    minWidth: 0,
    whiteSpace: "normal",
    wordBreak: "break-word",
  },
  "@media (max-width: 900px)": {
    parametersTable: {
      minWidth: "560px",
    },
    parameterCol: {
      width: "clamp(110px, 24vw, 210px)",
    },
    typeCol: {
      width: "clamp(80px, 16vw, 130px)",
    },
  },
  output: {
    border: `${tokens.strokeWidthThin} solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusSmall,
    padding: tokens.spacingHorizontalM,
    maxHeight: "45vh",
    overflowY: "auto",
  },
  outputTitle: {
    fontWeight: tokens.fontWeightSemibold,
  },
  outputHeader: {
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: tokens.spacingVerticalXS,
  },
  actionButtons: {
    display: "flex",
    alignItems: "center",
    columnGap: tokens.spacingHorizontalXS,
  },
});

export function ActionRunnerPage() {
  const actionRegistryDto = useActionRegistry();
  return <ActionsPageLoaded actionRegistry={actionRegistryDto} />;
}

export function ActionsPageLoaded({
  actionRegistry,
}: {
  actionRegistry: ActionRegistry;
}) {
  const styles = useActionPageStyles();
  const { t } = useAppI18n();
  const defaultGroupKey = actionRegistry.findFirstGroupKey();
  const defaultActionKey = defaultGroupKey
    ? actionRegistry.findFirstActionKey(defaultGroupKey)
    : undefined;
  const defaultPayload = actionRegistry.createPayloadTemplate(
    defaultGroupKey,
    defaultActionKey,
  );

  const [selectedAction, setSelectedAction] = useState<{
    groupKey: string | undefined;
    actionKey: string | undefined;
  }>({
    groupKey: defaultGroupKey,
    actionKey: defaultActionKey,
  });

  const handleActionSelectionChange = (
    groupKey: string | undefined,
    actionKey: string | undefined,
  ) => {
    setSelectedAction({ groupKey, actionKey });
  };

  const headerProps: ViewLayoutHeaderProps = {
    eyebrow: t("actionRunnerPage_eyebrow"),
    title: t("actionRunnerPage_title"),
    titleIcon: <CodeBlockRegular/>

  };

  return (
    <ViewLayoutContained
      contained={false}
      scrollable={false}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <div className={styles.root}>
        <div className={styles.splitArea}>
          <div className={styles.masterPane}>
            <ActionsTree
              actionRegistry={actionRegistry}
              defaultGroupKey={defaultGroupKey}
              defaultActionKey={defaultActionKey}
              onActionSelect={handleActionSelectionChange}
            />
          </div>
          <div className={styles.detailPane}>
            <ActionLaucher
              actionRegistry={actionRegistry}
              selectedGroupKey={selectedAction.groupKey}
              selectedActionKey={selectedAction.actionKey}
              defaultPayload={defaultPayload}
            />
          </div>
        </div>
      </div>
    </ViewLayoutContained>
  );
}

function ActionLaucher({
  actionRegistry,
  selectedGroupKey,
  selectedActionKey,
  defaultPayload,
}: {
  actionRegistry: ActionRegistry;
  selectedGroupKey: string | undefined;
  selectedActionKey: string | undefined;
  defaultPayload: string;
}) {
  const styles = useActionLauncherStyles();
  const { t } = useAppI18n();
  const [payload, setPayload] = useState<string>(defaultPayload);
  const [output, setOutput] = useState<ActionResp | null>(null);
  const [errorMessage, setErrorMessage] = useState<Problem | null>(null);

  const selectedActionDescriptor = useMemo(() => {
    return actionRegistry.findActionOptional(
      selectedGroupKey,
      selectedActionKey,
    );
  }, [actionRegistry, selectedGroupKey, selectedActionKey]);

  const payloadTemplate = useMemo(() => {
    return actionRegistry.createPayloadTemplate(
      selectedGroupKey,
      selectedActionKey,
    );
  }, [actionRegistry, selectedActionKey, selectedGroupKey]);

  useEffect(() => {
    setPayload(payloadTemplate);
  }, [payloadTemplate]);

  useEffect(() => {
    setOutput(null);
    setErrorMessage(null);
  }, [selectedGroupKey, selectedActionKey]);

  const toProblem = (problemJson: ProblemJson): Problem => {
    return new Problem(problemJson);
  };
  const toOutputText = (resp: ActionResp): string => {
    if (resp.contentType === "text") {
      return resp.text;
    }
    return JSON.stringify(resp.json, null, 2);
  };

  const handleSubmit = () => {
    if (!selectedGroupKey || !selectedActionKey) {
      setErrorMessage(
        toProblem({
          type: "action-runner/validation-error",
          title: t("actionRunnerPage_selectResourceAndActionError"),
        }),
      );
      return;
    }
    let parsedPayload;
    try {
      parsedPayload = JSON.parse(payload);
    } catch (e) {
      setErrorMessage(
        toProblem({
          type: "action-runner/invalid-payload",
          title: t("actionRunnerPage_invalidPayloadError", {
            details:
              e instanceof Error ? e.message : t("actionRunnerPage_unknownError"),
          }),
        }),
      );
      return;
    }
    setErrorMessage(null);

    executeAction(selectedGroupKey, selectedActionKey, parsedPayload)
      .then((data) => setOutput(data))
      .catch((err) => {
        const problem =
          err instanceof Problem
            ? err
            : toProblem({
                type: "action-runner/execution-error",
                title: t("actionRunnerPage_unknownError"),
                detail: err instanceof Error ? err.message : `${err}`,
              });
        setErrorMessage(problem);
        setOutput(null);
      });
  };

  const handleClear = () => {
    setOutput({ contentType: "text", text: "" });
  };
  const handleCopyOutput = () => {
    if (!output) {
      return;
    }
    navigator.clipboard.writeText(toOutputText(output)).catch((e) =>
      setErrorMessage(
        toProblem({
          type: "action-runner/copy-error",
          title: t("actionRunnerPage_copyOutputError"),
          detail: e instanceof Error ? e.message : `${e}`,
        }),
      ),
    );
  };
  const sortedParams = sortBy(
    selectedActionDescriptor?.parameters ?? [],
    (it) => it.order,
  );
  return (
    <div className={styles.root}>
      <div className={styles.titleRow}>
        <div className={styles.titleBlock}>
          <div className={styles.title}>
            {selectedActionKey ?? t("actionRunnerPage_noActionSelected")}
          </div>
          {selectedActionDescriptor &&
          selectedActionDescriptor.title !== selectedActionKey ? (
            <div className={styles.subtitle}>
              {selectedActionDescriptor.title}
            </div>
          ) : (
            ""
          )}
        </div>
        {selectedActionDescriptor ? (
          <div className={styles.securityRule}>
            <SecurityRuleBadge
              securityRule={selectedActionDescriptor.securityRule}
            />
          </div>
        ) : (
          ""
        )}
      </div>
      {selectedActionDescriptor ? (
        <div>
          <div className={styles.actionDescription}>
            <div className={styles.markdownCompact}>
              <Markdown value={selectedActionDescriptor.description} />
            </div>
          </div>
          {selectedActionDescriptor.parameters.length === 0 ? (
            <MissingInformation>
              {t("actionRunnerPage_noParametersRequired")}
            </MissingInformation>
          ) : (
            <div className={styles.parametersTableWrapper}>
              <Table size="small" className={styles.parametersTable}>
                <TableHeader>
                  <TableRow>
                    <TableHeaderCell className={styles.parameterCol}>
                      Parameter
                    </TableHeaderCell>
                    <TableHeaderCell className={styles.typeCol}>
                      Type
                    </TableHeaderCell>
                    <TableHeaderCell className={styles.descriptionCol}>
                      Description
                    </TableHeaderCell>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {sortedParams.map((parameter) => (
                    <TableRow key={parameter.name}>
                      <TableCell className={styles.parameterCol}>
                        <div>{parameter.name}</div>
                      </TableCell>
                      <TableCell className={styles.typeCol}>
                        {parameter.type} {parameter.optional ? "?" : ""}
                      </TableCell>
                      <TableCell className={styles.descriptionCol}>
                        {parameter.description ? (
                          <div className={styles.markdownCompact}>
                            <Markdown value={parameter.description} />
                          </div>
                        ) : (
                          "-"
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </div>
      ) : (
        <div>{t("actionRunnerPage_noActionSelected")}</div>
      )}
      <Field label={t("actionRunnerPage_payloadLabel")}>
        <Textarea
          placeholder={t("actionRunnerPage_payloadPlaceholder")}
          value={payload}
          onChange={(e) => setPayload(e.target.value)}
          rows={6}
        />
      </Field>
      <div className={styles.actionButtons}>
        <Button
          appearance="primary"
          icon={<CheckmarkRegular />}
          onClick={handleSubmit}
        >
          {t("actionRunnerPage_submit")}
        </Button>
        <Button
          appearance="secondary"
          icon={<DismissRegular />}
          onClick={handleClear}
        >
          {t("actionRunnerPage_clear")}
        </Button>
      </div>
      {errorMessage ? <ErrorBox error={errorMessage} /> : ""}
      {output && (
        <div>
          <div className={styles.outputHeader}>
            <div className={styles.outputTitle}>{t("actionRunnerPage_output")}</div>
            <Button
              appearance="secondary"
              icon={<CopyRegular />}
              onClick={handleCopyOutput}
            >
              {t("actionRunnerPage_copy_output")}
            </Button>
          </div>
          <pre className={styles.output}>
            <ActionOutput resp={output} />
          </pre>
        </div>
      )}
    </div>
  );
}

function ActionsTree({
  actionRegistry,
  defaultGroupKey,
  defaultActionKey,
  onActionSelect,
}: {
  actionRegistry: ActionRegistry;
  defaultGroupKey: string | undefined;
  defaultActionKey: string | undefined;
  onActionSelect: (
    groupKey: string | undefined,
    actionKey: string | undefined,
  ) => void;
}) {
  const styles = useActionTreeStyles();
  const [selectedGroupKey, setSelectedGroupKey] = useState<string | undefined>(
    defaultGroupKey,
  );
  const [selectedActionKey, setSelectedActionKey] = useState<
    string | undefined
  >(defaultActionKey);
  const [openGroupKeys, setOpenGroupKeys] = useState<TreeItemValue[]>(
    defaultGroupKey ? [defaultGroupKey] : [],
  );

  const selectedActionValue = useMemo(
    () =>
      selectedGroupKey && selectedActionKey
        ? `${selectedGroupKey}:${selectedActionKey}`
        : undefined,
    [selectedGroupKey, selectedActionKey],
  );
  const treeGroups = useMemo(
    () =>
      actionRegistry.actionGroupKeys
        .slice()
        .sort((a, b) => a.localeCompare(b))
        .map((groupKey) => ({
          groupKey,
          actions: actionRegistry
            .findActionDtoListByResource(groupKey)
            .slice()
            .sort((a, b) => a.key.localeCompare(b.key)),
        })),
    [actionRegistry],
  );

  const handleChangeActionGroup = (groupKey: string | undefined) => {
    const nextGroup =
      groupKey == undefined
        ? undefined
        : actionRegistry.existsGroup(groupKey)
          ? groupKey
          : undefined;
    const nextAction = nextGroup
      ? actionRegistry.findFirstActionKey(nextGroup)
      : undefined;
    setSelectedGroupKey(nextGroup);
    setSelectedActionKey(nextAction);
    setOpenGroupKeys((currentOpenGroupKeys) => {
      if (!nextGroup) {
        return currentOpenGroupKeys;
      }
      const hasGroupAlreadyOpen = currentOpenGroupKeys.some(
        (openItem) => openItem === nextGroup,
      );
      return hasGroupAlreadyOpen
        ? currentOpenGroupKeys
        : [...currentOpenGroupKeys, nextGroup];
    });
    onActionSelect(nextGroup, nextAction);
  };

  const handleSelectActionFromTree = (groupKey: string, actionKey: string) => {
    if (!actionRegistry.existsAction(groupKey, actionKey)) {
      setSelectedActionKey(undefined);
      onActionSelect(groupKey, undefined);
      return;
    }
    setSelectedGroupKey(groupKey);
    setSelectedActionKey(actionKey);
    setOpenGroupKeys((currentOpenGroupKeys) => {
      const hasGroupAlreadyOpen = currentOpenGroupKeys.some(
        (openItem) => openItem === groupKey,
      );
      return hasGroupAlreadyOpen
        ? currentOpenGroupKeys
        : [...currentOpenGroupKeys, groupKey];
    });
    onActionSelect(groupKey, actionKey);
  };

  return (
    <div className={styles.root}>
      <Tree
        aria-label="action-tree"
        openItems={openGroupKeys}
        onOpenChange={(_, data) => setOpenGroupKeys(Array.from(data.openItems))}
      >
        {treeGroups.map((group) => (
          <TreeItem
            key={group.groupKey}
            itemType="branch"
            value={group.groupKey}
          >
            <TreeItemLayout
              onClick={() => handleChangeActionGroup(group.groupKey)}
            >
              {group.groupKey}
            </TreeItemLayout>
            <Tree>
              {group.actions.map((action) => (
                <TreeItem
                  key={`${group.groupKey}:${action.key}`}
                  itemType="leaf"
                  value={`${group.groupKey}:${action.key}`}
                >
                  <TreeItemLayout
                    className={mergeClasses(
                      styles.actionItem,
                      selectedActionValue === `${group.groupKey}:${action.key}`
                        ? styles.actionItemSelected
                        : undefined,
                    )}
                    onClick={() =>
                      handleSelectActionFromTree(group.groupKey, action.key)
                    }
                  >
                    {action.key}
                  </TreeItemLayout>
                </TreeItem>
              ))}
            </Tree>
          </TreeItem>
        ))}
      </Tree>
    </div>
  );
}
