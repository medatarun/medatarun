import { type Messages } from "./Messages";
import { attributePageMessages } from "./fr/AttributePageMessages";
import { commandsPageMessages } from "./fr/CommandsPageMessages";
import { dashboardPageMessages } from "./fr/DashboardPageMessages";
import { entityPageMessages } from "./fr/EntityPageMessages";
import { errorBoundaryMessages } from "./fr/ErrorBoundaryMessages";
import { layoutMessages } from "./fr/LayoutMessages";
import { loginPageMessages } from "./fr/LoginPageMessages";
import { modelPageMessages } from "./fr/ModelPageMessages";
import { modelsPageMessages } from "./fr/ModelsPageMessages";
import { preferencesPageMessages } from "./fr/PreferencesPageMessages";
import { relationshipDescriptionMessages } from "./fr/RelationshipDescriptionMessages";
import { relationshipPageMessages } from "./fr/RelationshipPageMessages";
import { sessionMessages } from "./fr/SessionMessages";
import { tagEditMessages } from "./fr/TagEditMessages";
import { tagGroupEditMessages } from "./fr/TagGroupEditMessages";
import { tagGroupsPageMessages } from "./fr/TagGroupsPageMessages";
import { typePageMessages } from "./fr/TypePageMessages";

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
