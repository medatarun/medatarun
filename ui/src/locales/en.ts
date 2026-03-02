import { type Messages } from "./Messages";
import { actionPerformerViewMessages } from "./en/ActionPerformerViewMessages";
import { attributePageMessages } from "./en/AttributePageMessages";
import { commandsPageMessages } from "./en/CommandsPageMessages";
import { dashboardPageMessages } from "./en/DashboardPageMessages";
import { entityPageMessages } from "./en/EntityPageMessages";
import { errorBoundaryMessages } from "./en/ErrorBoundaryMessages";
import { layoutMessages } from "./en/LayoutMessages";
import { loginPageMessages } from "./en/LoginPageMessages";
import { modelPageMessages } from "./en/ModelPageMessages";
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

export const messages = {
  ...sessionMessages,
  ...attributePageMessages,
  ...actionPerformerViewMessages,
  ...commandsPageMessages,
  ...dashboardPageMessages,
  ...preferencesPageMessages,
  ...relationshipDescriptionMessages,
  ...relationshipsTableMessages,
  ...errorBoundaryMessages,
  ...layoutMessages,
  ...modelsPageMessages,
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
