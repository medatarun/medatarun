import { type Messages } from "./Messages";
import { actionPerformerViewMessages } from "./fr/ActionPerformerViewMessages";
import { adminActorPageMessages } from "./fr/AdminActorPageMessages";
import { adminActorsPageMessages } from "./fr/AdminActorsPageMessages";
import { adminDbDatasourceListPageMessages } from "./fr/AdminDbDatasourceListPageMessages";
import { adminDbDriverListPageMessages } from "./fr/AdminDbDriverListPageMessages";
import { adminUserPageMessages } from "./fr/AdminUserPageMessages";
import { adminUsersPageMessages } from "./fr/AdminUsersPageMessages";
import { authRolePageMessages } from "./fr/AuthRolePageMessages";
import { authRolesPageMessages } from "./fr/AuthRolesPageMessages";
import { attributeEditPageMessages } from "./fr/AttributeEditPageMessages";
import { actionPageMessage } from "./fr/ActionPageMessage";
import { dashboardPageMessages } from "./fr/DashboardPageMessages";
import { entityEditPageMessages } from "./fr/EntityEditPageMessages.ts";
import { errorBoundaryMessages } from "./fr/ErrorBoundaryMessages";
import { formValidationMessages } from "./fr/FormValidationMessages";
import { inlineEditRichTextLayoutMessages } from "./fr/InlineEditRichTextLayoutMessages";
import { inlineEditSingleLineLayoutMessages } from "./fr/InlineEditSingleLineLayoutMessages";
import { inlineEditTagsMessages } from "./fr/InlineEditTagsMessages";
import { layoutMessages } from "./fr/LayoutMessages";
import { loginPageMessages } from "./fr/LoginPageMessages";
import { modelPageMessages } from "./fr/ModelPageMessages";
import { modelComparePageMessages } from "./fr/ModelComparePageMessages";
import { modelHistoryPageMessages } from "./fr/ModelHistoryPageMessages";
import { modelReportsPageMessages } from "./fr/ModelReportsPageMessages";
import { modelListPageMessages } from "./fr/ModelListPageMessages.ts";
import { preferencesPageMessages } from "./fr/PreferencesPageMessages";
import { relationshipDescriptionMessages } from "./fr/RelationshipDescriptionMessages";
import { relationshipPageMessages } from "./fr/RelationshipPageMessages";
import { relationshipsTableMessages } from "./fr/RelationshipsTableMessages";
import { sessionMessages } from "./fr/SessionMessages";
import { tagEditMessages } from "./fr/TagEditMessages";
import { tagGroupEditMessages } from "./fr/TagGroupEditMessages";
import { tagGroupsPageMessages } from "./fr/TagGroupsPageMessages";
import { tagsTableMessages } from "./fr/TagsTableMessages";
import { typePageMessages } from "./fr/TypePageMessages";
import { typesTableMessages } from "./fr/TypesTableMessages";
import { menuMessages } from "./fr/MenuMessages.ts";

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
  ...relationshipPageMessages,
  ...tagGroupsPageMessages,
  ...tagGroupEditMessages,
  ...tagEditMessages,
  ...tagsTableMessages,
  ...typePageMessages,
  ...typesTableMessages,
} satisfies Messages;
