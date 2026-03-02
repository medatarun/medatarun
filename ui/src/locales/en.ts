import { type Messages } from "./Messages";
import { attributePageMessages } from "./en/AttributePageMessages";
import { commandsPageMessages } from "./en/CommandsPageMessages";
import { dashboardPageMessages } from "./en/DashboardPageMessages";
import { entityPageMessages } from "./en/EntityPageMessages";
import { errorBoundaryMessages } from "./en/ErrorBoundaryMessages";
import { layoutMessages } from "./en/LayoutMessages";
import { loginPageMessages } from "./en/LoginPageMessages";
import { modelPageMessages } from "./en/ModelPageMessages";
import { modelsPageMessages } from "./en/ModelsPageMessages";
import { preferencesPageMessages } from "./en/PreferencesPageMessages";
import { relationshipDescriptionMessages } from "./en/RelationshipDescriptionMessages";
import { relationshipPageMessages } from "./en/RelationshipPageMessages";
import { sessionMessages } from "./en/SessionMessages";
import { tagEditMessages } from "./en/TagEditMessages";
import { tagGroupEditMessages } from "./en/TagGroupEditMessages";
import { tagGroupsPageMessages } from "./en/TagGroupsPageMessages";
import { typePageMessages } from "./en/TypePageMessages";

export const messages = {
  ...sessionMessages,
  ...attributePageMessages,
  ...commandsPageMessages,
  ...dashboardPageMessages,
  ...preferencesPageMessages,
  ...relationshipDescriptionMessages,
  ...errorBoundaryMessages,
  ...layoutMessages,
  ...modelsPageMessages,
  ...modelPageMessages,
  ...entityPageMessages,
  ...loginPageMessages,
  ...relationshipPageMessages,
  ...tagGroupsPageMessages,
  ...tagGroupEditMessages,
  ...tagEditMessages,
  ...typePageMessages,
} satisfies Messages;
