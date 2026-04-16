import type { PropsWithChildren, ReactNode } from "react";
import {
  type ActionUILocation,
  useActionRegistry,
} from "@/business/action_registry";
import { Text, tokens } from "@fluentui/react-components";
import type {
  ActionDisplayedSubject,
  ActionPerformerRequestParams,
} from "@/components/business/actions";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";

export function SectionTitle({
  icon,
  location,
  actionParams,
  displayedSubject,
  children,
}: {
  icon: ReactNode;
  actionParams: ActionPerformerRequestParams;
  /**
   * Page subject propagated to action performer.
   * Keep it equal to the page displayed subject.
   */
  displayedSubject: ActionDisplayedSubject;
  location: ActionUILocation;
} & PropsWithChildren) {
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(location);

  return (
    <div
      style={{
        display: "flex",
        columnGap: tokens.spacingHorizontalS,
        alignItems: "center",
        justifyContent: "end",
        borderBottom: "1px solid #ccc",
        paddingBottom: tokens.spacingVerticalS,
        paddingRight: tokens.spacingVerticalS,
        marginTop: tokens.spacingVerticalXXXL,
        marginBottom: 0,
      }}
    >
      <div>
        <Text weight="semibold" style={{ display: "flex" }}>
          {icon}
        </Text>
      </div>
      <div>
        <Text weight="semibold">{children}</Text>
      </div>
      <div>
        <ActionMenuButton
          actionParams={actionParams}
          displayedSubject={displayedSubject}
          itemActions={actions}
        />
      </div>
    </div>
  );
}
