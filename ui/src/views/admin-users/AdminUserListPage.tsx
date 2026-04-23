import { useNavigate } from "@tanstack/react-router";
import type { ActionDescriptor } from "@/business/action_registry";
import { type UserInfoDto, useUserList } from "@/business/auth_user";
import { SectionTable } from "@/components/layout/SecionTable.tsx";
import { ViewLayoutContained } from "@/components/layout/ViewLayoutContained.tsx";
import {
  Caption1,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Text,
  tokens,
} from "@fluentui/react-components";
import { ErrorBox } from "@seij/common-ui";
import { toProblem } from "@seij/common-types";
import {
  type ActionCtx,
  createActionCtxVoid,
  displaySubjectNone,
} from "@/business/action-performer";
import { useAppI18n } from "@/services/appI18n.tsx";
import { sortBy } from "lodash-es";
import {
  ViewLayoutHeader,
  type ViewLayoutHeaderProps,
} from "@/components/layout/ViewLayoutHeader.tsx";
import { PersonRegular } from "@fluentui/react-icons";
import { ViewLayoutPageInfo } from "@/components/layout/ViewLayoutPageInfo.tsx";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";
import { createActionCtxUser } from "@/business/auth_user/user.actioncontexts.ts";
import { useActionRegistry } from "@/components/business/actions";

export function AdminUserListPage() {
  const { t } = useAppI18n();
  const navigate = useNavigate();
  const actionRegistry = useActionRegistry();
  const usersResult = useUserList();
  const itemActions = actionRegistry.findActionDescriptors([
    "user_enable",
    "user_disable",
    "user_change_fullname",
  ]);

  if (usersResult.isPending) return null;
  if (usersResult.error)
    return <ErrorBox error={toProblem(usersResult.error)} />;

  const userItems = sortBy(usersResult.data.items, (it) =>
    it.fullname.toLowerCase(),
  );
  const handleClickUser = (userId: string) => {
    navigate({ to: "/admin/users/$userId", params: { userId } });
  };

  const actionCtxPage = createActionCtxVoid();

  const actionCtxUser = (user: UserInfoDto) =>
    createActionCtxUser(user, displaySubjectNone);

  const headerProps: ViewLayoutHeaderProps = {
    eyebrow: t("adminUsersPage_eyebrow"),
    title: t("adminUsersPage_title"),
    titleIcon: <PersonRegular />,
    actions: {
      label: t("adminUsersPage_actions"),
      itemActions: actionRegistry.findActionDescriptors(["user_create"]),
      actionCtx: actionCtxPage,
    },
  };

  return (
    <ViewLayoutContained
      contained={true}
      scrollable={true}
      title={<ViewLayoutHeader {...headerProps} />}
    >
      <ViewLayoutPageInfo>
        <div>{t("adminUsersPage_description")}</div>
        <div style={{ marginTop: tokens.spacingVerticalS }}>
          {t("adminUsersPage_roleReminder")}
        </div>
      </ViewLayoutPageInfo>
      <SectionTable>
        <AdminUsersTable
          users={userItems}
          itemActions={itemActions}
          onClickUser={handleClickUser}
          actionCtxUser={actionCtxUser}
        />
      </SectionTable>
    </ViewLayoutContained>
  );
}

function AdminUsersTable({
  users,
  itemActions,
  onClickUser,
  actionCtxUser,
}: {
  users: UserInfoDto[];
  itemActions: ActionDescriptor[];
  onClickUser: (userId: string) => void;
  actionCtxUser: (user: UserInfoDto) => ActionCtx;
}) {
  const { t } = useAppI18n();

  if (users.length === 0) {
    return (
      <p style={{ paddingTop: tokens.spacingVerticalM }}>
        <Text italic>{t("adminUsersPage_empty")}</Text>
      </p>
    );
  }

  return (
    <Table>
      <TableBody>
        {users.map((user) => (
          <TableRow key={user.id}>
            <TableCell onClick={() => onClickUser(user.id)}>
              <div>
                {user.fullname}
                {user.disabledDate ? (
                  <Caption1 style={{ display: "inline" }}>
                    {" "}
                    - {t("adminUsersPage_disabled")}
                  </Caption1>
                ) : null}
              </div>
              <Caption1>{user.username}</Caption1>
            </TableCell>
            <TableCell style={{ width: "3em", textAlign: "right" }}>
              <ActionMenuButton
                itemActions={itemActions}
                actionCtx={actionCtxUser(user)}
              />
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
