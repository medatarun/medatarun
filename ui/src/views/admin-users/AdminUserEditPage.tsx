import { useNavigate } from "@tanstack/react-router";
import {
  ActionUILocations,
  useActionRegistry,
} from "@/business/action_registry";
import { useUserList } from "@/business/auth_user";
import {
  Breadcrumb,
  BreadcrumbButton,
  BreadcrumbDivider,
  BreadcrumbItem,
} from "@fluentui/react-components";
import { SectionPaper } from "@/components/layout/SectionPaper.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import { ErrorBox, InfoBox } from "@seij/common-ui";
import { formatLocalDateTime, toProblem } from "@seij/common-types";
import {
  createActionTemplateUser,
  createDisplayedSubjectUser,
} from "@/components/business/auth_user/user.actions.ts";
import { useAppI18n } from "@/services/appI18n.tsx";
import { PropertiesForm } from "@/components/layout/PropertiesForm.tsx";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { PersonRegular } from "@fluentui/react-icons";
import { ViewLayoutTechnicalInfos } from "@/components/layout/ViewLayoutTechnicalInfos.tsx";
import { createActionCtx } from "@/components/business/actions";

export function AdminUserEditPage({ userId }: { userId: string }) {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const userListResult = useUserList();

  if (userListResult.isPending) return null;
  if (userListResult.error)
    return <ErrorBox error={toProblem(userListResult.error)} />;

  const user = userListResult.data.items.find((it) => it.id === userId);
  if (!user) {
    return <InfoBox intent="warning">{t("adminUserPage_notFound")}</InfoBox>;
  }

  const actionCtxPage = createActionCtx({
    actionParams: createActionTemplateUser(user.username),
    displayedSubject: createDisplayedSubjectUser(user.username),
  });

  const breadcrumb = (
    <Breadcrumb size="small">
      <BreadcrumbItem>
        <BreadcrumbButton onClick={() => navigate({ to: "/admin/users" })}>
          {t("adminUserPage_breadcrumb")}
        </BreadcrumbButton>
      </BreadcrumbItem>
      <BreadcrumbDivider />
      <BreadcrumbItem>
        <BreadcrumbButton current={true}>
          {t("adminUserPage_eyebrow")}
        </BreadcrumbButton>
      </BreadcrumbItem>
    </Breadcrumb>
  );

  const headerProps: ViewLayoutHeaderProps = {
    breadcrumb: breadcrumb,
    title: user.fullname,
    titleIcon: <PersonRegular />,
    actions: {
      label: t("adminUserPage_actions"),
      itemActions: actionRegistry.findActions(ActionUILocations.user),
      actionCtx: actionCtxPage,
    },
  };

  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <SectionPaper>
        <PropertiesForm>
          <div>{t("adminUserPage_fullname")}</div>
          <div>{user.fullname}</div>

          <div>{t("adminUserPage_username")}</div>
          <div>{user.username}</div>

          <div>{t("adminUserPage_admin")}</div>
          <div>{String(user.admin)}</div>

          <div>{t("adminUserPage_status")}</div>
          <div>
            {user.disabledDate
              ? `${t("adminUserPage_statusDisabledAt")} ${formatLocalDateTime(user.disabledDate)}`
              : t("adminUserPage_statusActive")}
          </div>
        </PropertiesForm>
      </SectionPaper>
      <ViewLayoutTechnicalInfos
        id={user.id}
        idLabel={t("adminUserPage_identifier")}
      />
    </ViewLayoutContained>
  );
}
