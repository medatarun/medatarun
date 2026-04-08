import { type Messages } from "./Messages";
import { actionPerformerViewMessages } from "./en/ActionPerformerViewMessages";
import { adminActorPageMessages } from "./en/AdminActorPageMessages";
import { adminActorsPageMessages } from "./en/AdminActorsPageMessages";
import { adminDbDatasourceListPageMessages } from "./en/AdminDbDatasourceListPageMessages";
import { adminDbDriverListPageMessages } from "./en/AdminDbDriverListPageMessages";
import { adminUserPageMessages } from "./en/AdminUserPageMessages";
import { adminUsersPageMessages } from "./en/AdminUsersPageMessages";
import { authRolePageMessages } from "./en/AuthRolePageMessages";
import { authRolesPageMessages } from "./en/AuthRolesPageMessages";
import { attributeEditPageMessages } from "./en/AttributeEditPageMessages";
import { actionPageMessage } from "./en/ActionPageMessage";
import { dashboardPageMessages } from "./en/DashboardPageMessages";
import { entityEditPageMessages } from "./en/EntityEditPageMessages.ts";
import { errorBoundaryMessages } from "./en/ErrorBoundaryMessages";
import { formValidationMessages } from "./en/FormValidationMessages";
import { inlineEditRichTextLayoutMessages } from "./en/InlineEditRichTextLayoutMessages";
import { inlineEditSingleLineLayoutMessages } from "./en/InlineEditSingleLineLayoutMessages";
import { inlineEditTagsMessages } from "./en/InlineEditTagsMessages";
import { layoutMessages } from "./en/LayoutMessages";
import { loginPageMessages } from "./en/LoginPageMessages";
import { modelPageMessages } from "./en/ModelPageMessages";
import { modelComparePageMessages } from "./en/ModelComparePageMessages";
import { modelHistoryPageMessages } from "./en/ModelHistoryPageMessages";
import { modelReportsPageMessages } from "./en/ModelReportsPageMessages";
import { modelListPageMessages } from "./en/ModelListPageMessages.ts";
import { preferencesPageMessages } from "./en/PreferencesPageMessages";
import { relationshipDescriptionMessages } from "./en/RelationshipDescriptionMessages";
import { relationshipEditPageMessages } from "./en/RelationshipEditPageMessages.ts";
import { relationshipsTableMessages } from "./en/RelationshipsTableMessages";
import { sessionMessages } from "./en/SessionMessages";
import { tagEditMessages } from "./en/TagEditMessages";
import { tagGroupEditMessages } from "./en/TagGroupEditMessages";
import { tagGroupsPageMessages } from "./en/TagGroupsPageMessages";
import { tagsTableMessages } from "./en/TagsTableMessages";
import { typeEditPageMessages } from "./en/TypeEditPageMessages.ts";
import { typesTableMessages } from "./en/TypesTableMessages";
import { menuMessages } from "./en/MenuMessages.ts";

export const messages = {
  ...sessionMessages,
  ...attributeEditPageMessages,
  ...actionPerformerViewMessages,
  ...adminActorPageMessages,
  ...adminActorsPageMessages,
  ...adminDbDatasourceListPageMessages,
  ...adminDbDriverListPageMessages,
  ...adminUserPageMessages,
  ...adminUsersPageMessages,
  ...authRolePageMessages,
  ...authRolesPageMessages,
  ...actionPageMessage,
  ...dashboardPageMessages,
  ...preferencesPageMessages,
  ...relationshipDescriptionMessages,
  ...relationshipsTableMessages,
  ...errorBoundaryMessages,
  ...formValidationMessages,
  ...inlineEditRichTextLayoutMessages,
  ...inlineEditSingleLineLayoutMessages,
  ...inlineEditTagsMessages,
  ...layoutMessages,
  ...menuMessages,
  ...modelListPageMessages,
  ...modelComparePageMessages,
  ...modelHistoryPageMessages,
  ...modelPageMessages,
  ...modelReportsPageMessages,
  ...entityEditPageMessages,
  ...loginPageMessages,
  ...relationshipEditPageMessages,
  ...tagGroupsPageMessages,
  ...tagGroupEditMessages,
  ...tagEditMessages,
  ...tagsTableMessages,
  ...typeEditPageMessages,
  ...typesTableMessages,
} satisfies Messages;
