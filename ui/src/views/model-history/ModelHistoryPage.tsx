import { useNavigate } from "@tanstack/react-router";
import { Model } from "@medatarun/ui/business/model";
import {
  useModel,
  useModelHistoryVersionChanges,
  useModelHistoryVersions,
} from "@medatarun/ui/components/business/model";
import { ModelIcon } from "@medatarun/ui/components/business/model/model.icons.tsx";
import { ViewLayoutContained } from "@medatarun/ui/components/layout/ViewLayoutContained.tsx";
import { useAppI18n } from "@medatarun/ui/services/appI18n.tsx";
import { ModelHistoryVersionInput } from "@medatarun/ui/views/model-history/components/ModelHistoryVersionInput.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  tokens,
} from "@fluentui/react-components";
import { useMemo, useState } from "react";
import { ContainedHumanReadable } from "@medatarun/ui/components/layout/Contained.tsx";
import { FormField, Loader } from "@seij/common-ui";
import { ModelHistoryChanges } from "@medatarun/ui/views/model-history/components/ModelHistoryChanges.tsx";
import {
  ModelContext,
  useModelContext,
} from "@medatarun/ui/components/business/model/ModelContext.tsx";
import { sortBy } from "lodash-es";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@medatarun/ui/components/layout/ViewLayoutHeader";
import { HistoryRegular } from "@fluentui/react-icons";

export function ModelHistoryPage({ modelId }: { modelId: string }) {
  const navigate = useNavigate();
  const { data: modelDto } = useModel(modelId);

  const { t } = useAppI18n();

  const model = modelDto ? new Model(modelDto) : null;

  const handleClickModel = () => {
    if (!model) return;
    navigate({
      to: "/model/$modelId",
      params: { modelId: model.id },
    });
  };

  const breadCrumb = (
    <Breadcrumb size="small">
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<ModelIcon authority={model?.authority} />}
          onClick={handleClickModel}
        >
          {model?.nameOrKey ?? ""}
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider />
    </Breadcrumb>
  );

  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: breadCrumb,
    title: t("modelHistoryPage_title"),
    titleIcon: <HistoryRegular />,
  };

  return (
    <ViewLayoutContained
      scrollable={true}
      contained={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      {model ? (
        <ModelContext value={model}>
          <ModelHistoryPageLoaded />
        </ModelContext>
      ) : (
        <Loader loading={true} />
      )}
    </ViewLayoutContained>
  );
}

function ModelHistoryPageLoaded() {
  const model = useModelContext();
  const modelId = model.id;
  const { data: versionsDto } = useModelHistoryVersions(modelId);
  const { t } = useAppI18n();
  const [selectedVersion, setSelectedVersion] = useState<string | null>(null);
  const { data: changesDto } = useModelHistoryVersionChanges(
    modelId,
    selectedVersion,
  );
  const items = useMemo(
    () =>
      sortBy(changesDto?.items ?? [], (it) => it.eventSequenceNumber).reverse(),
    [changesDto?.items],
  );
  return (
    <ContainedHumanReadable>
      <FormField label={t("modelHistoryPage_versionsTitle")}>
        <ModelHistoryVersionInput
          versions={versionsDto?.items ?? []}
          value={selectedVersion}
          onChange={setSelectedVersion}
        />
      </FormField>
      <div style={{ marginTop: tokens.spacingVerticalM }}>
        <div style={{ marginBottom: tokens.spacingVerticalM }}>
          {t("modelHistoryPage_changesTitle")}
        </div>
        <ModelHistoryChanges items={items} />
      </div>
    </ContainedHumanReadable>
  );
}
