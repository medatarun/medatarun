import { type Messages } from "./Messages";
import { actionPerformerViewMessages } from "./en/ActionPerformerViewMessages";
import { authRolePageMessages } from "./en/AuthRolePageMessages";
import { authRolesPageMessages } from "./en/AuthRolesPageMessages";
import { attributePageMessages } from "./en/AttributePageMessages";
import { commandsPageMessages } from "./en/CommandsPageMessages";
import { dashboardPageMessages } from "./en/DashboardPageMessages";
import { entityPageMessages } from "./en/EntityPageMessages";
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
import { modelsPageMessages } from "./en/ModelsPageMessages";
import { preferencesPageMessages } from "./en/PreferencesPageMessages";
import { relationshipDescriptionMessages } from "./en/RelationshipDescriptionMessages";
import { relationshipPageMessages } from "./en/RelationshipPageMessages";
import { relationshipsTableMessages } from "./en/RelationshipsTableMessages";
import { sessionMessages } from "./en/SessionMessages";
import { tagEditMessages } from "./en/TagEditMessages";
import { tagGroupEditMessages } from "./en/TagGroupEditMessages";
import { tagGroupsPageMessages } from "./en/TagGroupsPageMessages";
import { tagsTableMessages } from "./en/TagsTableMessages";
import { typePageMessages } from "./en/TypePageMessages";
import { typesTableMessages } from "./en/TypesTableMessages";
import { menuMessages } from "./en/MenuMessages.ts";

export const messages = {
  ...sessionMessages,
  ...attributePageMessages,
  ...actionPerformerViewMessages,
  ...authRolePageMessages,
  ...authRolesPageMessages,
  ...commandsPageMessages,
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
