import { Link, useNavigate } from "@tanstack/react-router";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import {
  createActionCtxType,
  createDisplayedSubjectType,
  Model,
  type TypeDto,
  useModel,
  useTypeUpdateDescription,
  useTypeUpdateKey,
  useTypeUpdateName,
} from "@/business/model";
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
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";

import { useDetailLevelContext } from "@/components/business/DetailLevelContext.tsx";
import { PropertiesForm } from "@/components/layout/PropertiesForm.tsx";
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
  const actions = actionRegistry.findActions(ActionUILocations.type);
  const typeUpdateName = useTypeUpdateName();
  const typeUpdateDescription = useTypeUpdateDescription();
  const { t } = useAppI18n();

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
        <BreadcrumbButton icon={<ModelIcon />} onClick={handleClickModel}>
          {model.nameOrKeyWithAuthorityEmoji}
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
      <InlineEditSingleLine value={type.name ?? ""} onChange={handleChangeName}>
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
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <SectionPaper>
        <TypeOverview model={model} type={type} />
      </SectionPaper>
      <SectionPaper topspacing="XXXL" nopadding>
        <InlineEditDescription
          value={type.description}
          placeholder={t("typeEditPage_descriptionPlaceholder")}
          onChange={(v) =>
            typeUpdateDescription.mutateAsync({
              modelId: model.id,
              typeId: type.id,
              value: v,
            })
          }
        />
      </SectionPaper>
    </ViewLayoutContained>
  );
}

export function TypeOverview({ type, model }: { type: TypeDto; model: Model }) {
  const typeUpdateKey = useTypeUpdateKey();
  const { isDetailLevelTech } = useDetailLevelContext();
  const { t } = useAppI18n();

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
        <div>
          <Text>{t("typeEditPage_keyLabel")}</Text>
        </div>
      )}
      {isDetailLevelTech && (
        <InlineEditSingleLine value={type.key} onChange={handleChangeKey}>
          <Text>
            <code>{type.key}</code>
          </Text>
        </InlineEditSingleLine>
      )}
      <div>
        <Text>{t("typeEditPage_fromModelLabel")}</Text>
      </div>
      <div>
        <Link to="/model/$modelId" params={{ modelId: model.id }}>
          {model.nameOrKey}
        </Link>
      </div>
      {isDetailLevelTech && (
        <div>
          <Text>{t("typeEditPage_identifierLabel")}</Text>
        </div>
      )}
      {isDetailLevelTech && (
        <div>
          <Text>
            <code>{type.id}</code>
          </Text>
        </div>
      )}
    </PropertiesForm>
  );
}
