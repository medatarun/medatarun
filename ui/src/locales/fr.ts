import { type Messages } from "./Messages";
import { actionPerformerViewMessages } from "./fr/ActionPerformerViewMessages";
import { adminActorPageMessages } from "./fr/AdminActorPageMessages";
import { adminActorsPageMessages } from "./fr/AdminActorsPageMessages";
import { adminUserPageMessages } from "./fr/AdminUserPageMessages";
import { adminUsersPageMessages } from "./fr/AdminUsersPageMessages";
import { authRolePageMessages } from "./fr/AuthRolePageMessages";
import { authRolesPageMessages } from "./fr/AuthRolesPageMessages";
import { attributePageMessages } from "./fr/AttributePageMessages";
import { actionPageMessage } from "./fr/ActionPageMessage";
import { dashboardPageMessages } from "./fr/DashboardPageMessages";
import { entityPageMessages } from "./fr/EntityPageMessages";
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
import { modelsPageMessages } from "./fr/ModelsPageMessages";
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
  ...attributePageMessages,
  ...actionPerformerViewMessages,
  ...adminActorPageMessages,
  ...adminActorsPageMessages,
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
  ...modelsPageMessages,
  ...modelComparePageMessages,
  ...modelHistoryPageMessages,
  ...modelPageMessages,
  ...modelReportsPageMessages,
  ...entityPageMessages,
  ...loginPageMessages,
  ...relationshipPageMessages,
  ...tagGroupsPageMessages,
  ...tagGroupEditMessages,
  ...tagEditMessages,
  ...tagsTableMessages,
  ...typePageMessages,
  ...typesTableMessages,
} satisfies Messages;
