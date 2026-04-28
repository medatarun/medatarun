import { useNavigate } from "@tanstack/react-router";
import {
  createActionCtxType,
  createDisplayedSubjectType,
  Model,
  type TypeDto,
} from "@/business/model";
import {
  useModel,
  useTypeUpdateDescription,
  useTypeUpdateKey,
  useTypeUpdateName,
} from "@/components/business/model";
import { ModelContext } from "@/components/business/model/ModelContext.tsx";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
  Text,
  tokens,
} from "@fluentui/react-components";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";

import { useDetailLevelContext } from "@/components/business/detail-level";
import {
  PropertiesForm,
  PropertyLabel,
  PropertyValue,
} from "@/components/layout/PropertiesForm.tsx";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import { InlineEditDescription } from "@/components/core/InlineEditDescription.tsx";
import { useMemo } from "react";
import { InlineEditSingleLine } from "@/components/core/InlineEditSingleLine.tsx";
import {
  ModelIcon,
  TypeIcon,
} from "@/components/business/model/model.icons.tsx";
import { useAppI18n } from "@/services/appI18n.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { useSecurityContext } from "@/components/business/security";
import { ViewLayoutTechnicalInfos } from "@/components/layout/ViewLayoutTechnicalInfos.tsx";
import { useActionRegistry } from "@/components/business/actions";

export function TypeEditPage({
  modelId,
  typeId,
}: {
  modelId: string;
  typeId: string;
}) {
  const { t } = useAppI18n();
  const { data: modelDto } = useModel(modelId);
  const model = useMemo(
    () => (modelDto ? new Model(modelDto) : null),
    [modelDto],
  );

  if (!modelDto) return null;
  if (!model) return null;

  const type = model.findTypeDto(typeId);
  if (!type) return <ErrorBox error={toProblem(t("typeEditPage_notFound"))} />;

  return (
    <ModelContext value={model}>
      <TypeView model={model} type={type} />
    </ModelContext>
  );
}

function TypeView({ model, type }: { type: TypeDto; model: Model }) {
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActionDescriptors([
    "type_update_key",
    "type_delete",
  ]);
  const typeUpdateName = useTypeUpdateName();
  const typeUpdateDescription = useTypeUpdateDescription();
  const { t } = useAppI18n();
  const sec = useSecurityContext();

  const updateNameDisabled = !sec.canExecuteAction("type_update_name");
  const updateDescriptionDisabled = !sec.canExecuteAction(
    "type_update_description",
  );

  const handleClickModel = () => {
    navigate({
      to: "/model/$modelId",
      params: { modelId: model.id },
    });
  };

  const handleChangeName = (value: string) => {
    return typeUpdateName.mutateAsync({
      modelId: model.id,
      typeId: type.id,
      value: value,
    });
  };

  const displayedSubject = createDisplayedSubjectType(model.id, type.id);
  const actionCtxPage = createActionCtxType(model, type, displayedSubject);

  const breadcrumb = (
    <Breadcrumb size="small">
      <BreadcrumbItem>
        <BreadcrumbButton
          icon={<ModelIcon authority={model.authority} />}
          onClick={handleClickModel}
        >
          {model.nameOrKey}
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider />
      <BreadcrumbItem>
        <BreadcrumbButton current={true}>
          {t("typeEditPage_eyebrow")}
        </BreadcrumbButton>
      </BreadcrumbItem>
    </Breadcrumb>
  );

  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: breadcrumb,
    title: (
      <InlineEditSingleLine
        value={type.name ?? ""}
        disabled={updateNameDisabled}
        onChange={handleChangeName}
      >
        {type.name ? (
          model.findTypeNameOrKey(type.id)
        ) : (
          <span
            style={{
              color: tokens.colorNeutralForeground4,
              fontStyle: "italic",
            }}
          >
            {model.findTypeNameOrKey(type.id)}
          </span>
        )}{" "}
      </InlineEditSingleLine>
    ),
    titleIcon: <TypeIcon />,
    actions: {
      label: t("typeEditPage_actions"),
      itemActions: actions,
      actionCtx: actionCtxPage,
    },
  };

  return (
    <ViewLayoutContained
      scrollable={true}
      contained={true}
      verticalSpacing={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <TypeOverview model={model} type={type} />

      <InlineEditDescription
        value={type.description}
        disabled={updateDescriptionDisabled}
        placeholder={t("typeEditPage_descriptionPlaceholder")}
        onChange={(v) =>
          typeUpdateDescription.mutateAsync({
            modelId: model.id,
            typeId: type.id,
            value: v,
          })
        }
      />

      <ViewLayoutTechnicalInfos
        technicalKey={type.key}
        keyLabel={t("typeEditPage_keyLabel")}
        id={type.id}
        idLabel={t("typeEditPage_identifierLabel")}
      />
    </ViewLayoutContained>
  );
}

export function TypeOverview({ type, model }: { type: TypeDto; model: Model }) {
  const typeUpdateKey = useTypeUpdateKey();
  const { isDetailLevelTech } = useDetailLevelContext();
  const { t } = useAppI18n();
  const sec = useSecurityContext();

  const updatekeyDisabled = !sec.canExecuteAction("type_update_key");

  const handleChangeKey = (value: string) => {
    return typeUpdateKey.mutateAsync({
      modelId: model.id,
      typeId: type.id,
      value: value,
    });
  };

  return (
    <PropertiesForm>
      {isDetailLevelTech && (
        <PropertyLabel>{t("typeEditPage_keyLabel")}</PropertyLabel>
      )}
      {isDetailLevelTech && (
        <PropertyValue>
          <InlineEditSingleLine
            value={type.key}
            disabled={updatekeyDisabled}
            onChange={handleChangeKey}
          >
            <Text>
              <code>{type.key}</code>
            </Text>
          </InlineEditSingleLine>
        </PropertyValue>
      )}
    </PropertiesForm>
  );
}
