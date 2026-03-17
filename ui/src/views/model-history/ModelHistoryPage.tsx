import {useNavigate} from "@tanstack/react-router";
import {Model, useModel, useModelHistoryVersionChanges, useModelHistoryVersions,} from "@/business/model";
import {ModelIcon} from "@/components/business/model/model.icons.tsx";
import {ViewTitle} from "@/components/core/ViewTitle.tsx";
import {ViewLayoutContained} from "@/components/layout/ViewLayoutContained.tsx";
import {useAppI18n} from "@/services/appI18n.tsx";
import {ModelHistoryVersionInput} from "@/views/model-history/components/ModelHistoryVersionInput.tsx";
import {Breadcrumb, BreadcrumbButton, BreadcrumbDivider, BreadcrumbItem,} from "@fluentui/react-components";
import {useState} from "react";
import {ContainedHumanReadable} from "@/components/layout/Contained.tsx";
import {FormField} from "@seij/common-ui";
import {ModelHistoryChanges} from "@/views/model-history/components/ModelHistoryChanges.tsx";

export function ModelHistoryPage({modelId}: { modelId: string }) {
  const navigate = useNavigate();
  const {data: modelDto} = useModel(modelId);
  const {data: versionsDto} = useModelHistoryVersions(modelId);
  const {t} = useAppI18n();
  const [selectedVersion, setSelectedVersion] = useState<string | null>(null);
  const {data: changesDto} = useModelHistoryVersionChanges(modelId, selectedVersion);

  if (!modelDto) return null;

  const model = new Model(modelDto);

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: {modelId: model.id},
    });
  };

  return (
    <ViewLayoutContained
      title={
        <div>
          <Breadcrumb style={{marginLeft: "-22px"}} size="small">
            <BreadcrumbItem>
              <BreadcrumbButton icon={<ModelIcon/>} onClick={handleClickModel}>
                {model.nameOrKeyWithAuthorityEmoji}
              </BreadcrumbButton>
            </BreadcrumbItem>
            <BreadcrumbDivider/>
          </Breadcrumb>
          <ViewTitle eyebrow="">{t("modelHistoryPage_title")}</ViewTitle>
        </div>
      }
    >
      <ContainedHumanReadable>
        <FormField label={t("modelHistoryPage_versionsTitle")}>
          <ModelHistoryVersionInput
            versions={versionsDto?.items ?? []}
            value={selectedVersion}
            onChange={setSelectedVersion}
          />
        </FormField>

        <div>
          <div>{t("modelHistoryPage_changesTitle")}</div>
          <ModelHistoryChanges items={changesDto?.items ?? []}/>
        </div>
      </ContainedHumanReadable>
    </ViewLayoutContained>
  );
}
