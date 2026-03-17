import { useNavigate } from "@tanstack/react-router";
import { Model, useModel } from "@/business/model";
import { ModelIcon } from "@/components/business/model/model.icons.tsx";
import { ViewTitle } from "@/components/core/ViewTitle.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Text,
} from "@fluentui/react-components";

export function ModelHistoryPage({ modelId }: { modelId: string }) {
  const navigate = useNavigate();
  const { data: modelDto } = useModel(modelId);
  const { t } = useAppI18n();

  if (!modelDto) return null;

  const model = new Model(modelDto);

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: { modelId: model.id },
    });
  };

  return (
    <ViewLayoutContained
      title={
        <div>
          <Breadcrumb style={{ marginLeft: "-22px" }} size="small">
            <BreadcrumbItem>
              <BreadcrumbButton icon={<ModelIcon />} onClick={handleClickModel}>
                {model.nameOrKeyWithAuthorityEmoji}
              </BreadcrumbButton>
            </BreadcrumbItem>
            <BreadcrumbDivider />
          </Breadcrumb>
          <ViewTitle eyebrow="">{t("modelHistoryPage_title")}</ViewTitle>
        </div>
      }
    >
      <Text>Model history todo</Text>
    </ViewLayoutContained>
  );
}
