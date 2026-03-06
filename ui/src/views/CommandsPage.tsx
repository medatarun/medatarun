import {Fragment, useMemo, useState} from "react";
import {type ActionResp, executeAction} from "@/business/action_runner";
import {ActionRegistry, useActionRegistry} from "@/business/action_registry";
import {ActionOutput} from "@/components/business/actions/ActionOutput.tsx";
import {ViewLayoutContained} from "@/components/layout/ViewLayoutContained.tsx";
import {ViewTitle} from "@/components/core/ViewTitle.tsx";
import {
  Field,
  makeStyles,
  mergeClasses,
  Textarea,
  tokens,
  Tree,
  TreeItem,
  TreeItemLayout,
  type TreeItemValue,
} from "@fluentui/react-components";
import {Button} from "@seij/common-ui";
import {useAppI18n} from "@/services/appI18n.tsx";
import {ContainedMixedScrolling, ContainedScrollable} from "@/components/layout/Contained.tsx";

const useSelectionStyles = makeStyles({
  actionItem: {
    fontWeight: tokens.fontWeightRegular,
  },
  actionItemSelected: {
    fontWeight: tokens.fontWeightSemibold,
    backgroundColor: tokens.colorNeutralBackground1Selected,
    borderRadius: tokens.borderRadiusSmall,
  },
});

export function CommandsPage() {
  const commandRegistryDto = useActionRegistry();
  return <CommandsPageLoaded actionRegistry={commandRegistryDto}/>;
}

export function CommandsPageLoaded({
                                     actionRegistry,
                                   }: {
  actionRegistry: ActionRegistry;
}) {
  const {t} = useAppI18n();
  const defaultGroupKey = actionRegistry.findFirstGroupKey();
  const defaultActionKey = defaultGroupKey
    ? actionRegistry.findFirstActionKey(defaultGroupKey)
    : undefined;
  const defaultPayload = actionRegistry.createPayloadTemplate(
    defaultGroupKey,
    defaultActionKey,
  );

  const [selectedCommand, setSelectedCommand] = useState<{
    groupKey: string | undefined;
    actionKey: string | undefined;
  }>({
    groupKey: defaultGroupKey,
    actionKey: defaultActionKey,
  });

  const [payload, setPayload] = useState<string>(defaultPayload);
  const [output, setOutput] = useState<ActionResp | null>(null);
  const [errorMessage, setErrorMessage] = useState<string>("");

  const selectedActionDescriptor = useMemo(() => {
    return actionRegistry.findActionOptional(
      selectedCommand.groupKey,
      selectedCommand.actionKey,
    );
  }, [selectedCommand.groupKey, selectedCommand.actionKey, actionRegistry]);

  const handleSubmit = () => {
    if (!selectedCommand.groupKey || !selectedCommand.actionKey) {
      setErrorMessage(t("commandsPage_selectResourceAndActionError"));
      return;
    }
    let parsedPayload;
    try {
      parsedPayload = JSON.parse(payload);
    } catch (e) {
      setErrorMessage(
        t("commandsPage_invalidPayloadError", {
          details:
            e instanceof Error ? e.message : t("commandsPage_unknownError"),
        }),
      );
      return;
    }
    setErrorMessage("");

    executeAction(
      selectedCommand.groupKey,
      selectedCommand.actionKey,
      parsedPayload,
    )
      .then((data) => setOutput(data))
      .catch((err) =>
        setOutput({contentType: "json", json: {error: err.toString()}}),
      );
  };
  const handleClear = () => {
    setOutput({contentType: "text", text: ""});
  };

  const handleActionSelectionChange = (
    groupKey: string | undefined,
    actionKey: string | undefined,
  ) => {
    setSelectedCommand({groupKey, actionKey});
    setPayload(actionRegistry.createPayloadTemplate(groupKey, actionKey));
  };

  return (
    <ViewLayoutContained title={t("commandsPage_title")}>
      <ViewTitle eyebrow={t("commandsPage_eyebrow")}>
        {t("commandsPage_title")}
      </ViewTitle>
      <ContainedMixedScrolling>
        <ContainedScrollable>
          <div></div>
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "minmax(100px, 1fr) minmax(0, 4fr)",
              columnGap: "1em",
            }}
          >
            <div>
              <CommandsActionTree
                actionRegistry={actionRegistry}
                groupLabel={t("commandsPage_groupLabel")}
                actionLabel={t("commandsPage_actionLabel")}
                defaultGroupKey={defaultGroupKey}
                defaultActionKey={defaultActionKey}
                onActionSelect={handleActionSelectionChange}
              />
            </div>
            <div style={{display: "grid", rowGap: "1em"}}>
              {selectedActionDescriptor ? (
                <div>
                  <div style={{marginBottom: "0.5em"}}>
                    {selectedActionDescriptor.description}
                  </div>
                  <div
                    style={{
                      display: "grid",
                      gridTemplateColumns: "auto auto",
                      columnGap: "1em",
                      rowGap: "0.5em",
                    }}
                  >
                    {selectedActionDescriptor.parameters.map((parameter) => (
                      <Fragment key={parameter.name}>
                        <div>{parameter.name}</div>
                        <div>
                          {parameter.type} {parameter.optional ? "?" : ""}
                        </div>
                      </Fragment>
                    ))}
                  </div>
                </div>
              ) : (
                <div>{t("commandsPage_noActionSelected")}</div>
              )}
              <Field label={t("commandsPage_payloadLabel")}>
                <Textarea
                  placeholder={t("commandsPage_payloadPlaceholder")}
                  value={payload}
                  onChange={(e) => setPayload(e.target.value)}
                  rows={6}
                />
              </Field>

              <div>
                <Button variant="primary" onClick={handleSubmit}>
                  {t("commandsPage_submit")}
                </Button>
                <Button variant="secondary" onClick={handleClear}>
                  {t("commandsPage_clear")}
                </Button>
              </div>
              {errorMessage ? (
                <div style={{color: "red"}}>{errorMessage}</div>
              ) : (
                ""
              )}
              {output && (
                <pre style={{border: "1px solid green", padding: "1em"}}>
                  <ActionOutput resp={output}/>
                </pre>
              )}
            </div>
          </div>
        </ContainedScrollable>
      </ContainedMixedScrolling>
    </ViewLayoutContained>
  );
}


function CommandsActionTree(
  {
    actionRegistry,
    groupLabel,
    actionLabel,
    defaultGroupKey,
    defaultActionKey,
    onActionSelect,
  }: {
    actionRegistry: ActionRegistry;
    groupLabel: string;
    actionLabel: string;
    defaultGroupKey: string | undefined;
    defaultActionKey: string | undefined;
    onActionSelect: (
      groupKey: string | undefined,
      actionKey: string | undefined,
    ) => void;
  }) {
  const selectionStyles = useSelectionStyles();
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
    <Field label={`${groupLabel} / ${actionLabel}`}>
      <Tree
        aria-label="command-tree"
        openItems={openGroupKeys}
        onOpenChange={(_, data) => setOpenGroupKeys(Array.from(data.openItems))}
      >
        {treeGroups.map((group) => (
          <TreeItem key={group.groupKey} itemType="branch" value={group.groupKey}>
            <TreeItemLayout onClick={() => handleChangeActionGroup(group.groupKey)}>
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
                      selectionStyles.actionItem,
                      selectedActionValue === `${group.groupKey}:${action.key}`
                        ? selectionStyles.actionItemSelected
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
    </Field>
  );
}
