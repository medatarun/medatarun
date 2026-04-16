import { ModelCard } from "@/components/business/model/ModelCard.tsx";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import {
  Model,
  type ModelSummaryDto,
  useModelSummaries,
} from "@/business/model";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { InfoLabel, tokens } from "@fluentui/react-components";
import { createActionTemplateGeneral } from "@/components/business/model/model.actions.ts";
import { ModelIcon } from "@/components/business/model/model.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import { displaySubjectNone } from "@/components/business/actions";
import { SectionTitle } from "@/components/layout/SectionTitle.tsx";
import { SectionCards } from "@/components/layout/SectionCards.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";

export function ModelListPage({
  onClickModel,
}: {
  onClickModel: (modelId: string) => void;
}) {
  const { data = [] } = useModelSummaries();
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(ActionUILocations.models);
  const { t } = useAppI18n();
  const canonicalModels = data.filter(
    (model) => model.authority === "canonical",
  );
  const systemModels = data.filter((model) => model.authority !== "canonical");

  const headerProps: ViewLayoutHeaderProps = {
    title: t("modelListPage_title"),
    titleIcon: <ModelIcon />,
    actions: {
      label: t("modelListPage_actions"),
      itemActions: actions,
      actionParams: createActionTemplateGeneral(),
      displayedSubject: displaySubjectNone,
    },
  };

  return (
    <ViewLayoutContained
      title={<ViewLayoutHeader {...headerProps} />}
      contained={true}
      scrollable={true}
    >
      {data.length == 0 ? t("modelListPage_empty") : null}
      {canonicalModels.length > 0 && (
        <ModelsSection
          title={`${Model.authorityEmoji("canonical")} ${t("modelListPage_canonicalTitle")}`}
          titleInfo={t("modelListPage_canonicalInfo")}
          models={canonicalModels}
          onClickModel={onClickModel}
        />
      )}
      {systemModels.length > 0 && (
        <ModelsSection
          title={`${Model.authorityEmoji("system")} ${t("modelListPage_systemTitle")}`}
          titleInfo={t("modelListPage_systemInfo")}
          models={systemModels}
          onClickModel={onClickModel}
        />
      )}
    </ViewLayoutContained>
  );
}

function ModelsSection({
  title,
  titleInfo,
  models,
  onClickModel,
}: {
  title: string;
  titleInfo: string;
  models: ModelSummaryDto[];
  onClickModel: (modelId: string) => void;
}) {
  return (
    <>
      <SectionTitle
        icon={undefined}
        actionParams={createActionTemplateGeneral()}
        displayedSubject={displaySubjectNone}
        location={ActionUILocations.none}
      >
        <span
          style={{
            display: "inline-flex",
            alignItems: "center",
            gap: tokens.spacingHorizontalXS,
          }}
        >
          {title}
          <InfoLabel info={titleInfo}></InfoLabel>
        </span>
      </SectionTitle>
      <SectionCards>
        <div
          style={{
            display: "flex",
            justifyContent: "left",
            columnGap: tokens.spacingVerticalM,
            rowGap: tokens.spacingVerticalM,
            flexWrap: "wrap",
            marginTop: tokens.spacingVerticalM,
          }}
        >
          {models.map((model) => (
            <ModelCard key={model.id} model={model} onClick={onClickModel} />
          ))}
        </div>
      </SectionCards>
    </>
  );
}
