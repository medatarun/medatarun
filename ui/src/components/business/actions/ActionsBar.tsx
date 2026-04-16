import {
  ActionDescriptor,
  type ActionUILocation,
  useActionRegistry,
} from "@/business/action_registry";
import { useActionPerformer } from "./ActionPerformerHook.tsx";
import { Button, ButtonBar } from "@seij/common-ui";
import type { ComponentProps } from "react";
import {
  type ActionDisplayedSubject,
  type ActionPerformerRequestParams,
  displaySubjectNone,
} from "./ActionPerformerRequest.tsx";

type ActionBarProps = {
  location: ActionUILocation;
  params?: ActionPerformerRequestParams;
  variant?: ComponentProps<typeof ButtonBar>["variant"];
  displayedSubject?: ActionDisplayedSubject;
};

export const ActionsBar = ({
  location,
  params = {},
  variant,
  displayedSubject,
}: ActionBarProps) => {
  const actionRegistry = useActionRegistry();
  const actions = actionRegistry.findActions(location);
  return (
    <ButtonBar variant={variant}>
      {actions.map((it) => (
        <ActionButton
          key={it.path}
          location={location}
          action={it}
          params={params}
        />
      ))}
    </ButtonBar>
  );
};

export const ActionButton = ({
  action,
  params,
  displayedSubject,
}: {
  location: string;
  action: ActionDescriptor;
  params: ActionPerformerRequestParams;
  displayedSubject?: ActionDisplayedSubject;
}) => {
  const { performAction, state } = useActionPerformer();
  const disabled = state.kind !== "idle";

  const handleClick = async () => {
    try {
      performAction({
        actionKey: action.key,
        actionGroupKey: action.actionGroupKey,
        ctx: {
          actionParams: params,
          displayedSubject: displayedSubject ?? displaySubjectNone,
        },
      });
    } catch (e) {
      // We don't manage errors here
      console.error(
        "Error occurred and had not been property processed by action system",
        e,
      );
    }
  };
  return (
    <Button variant="secondary" disabled={disabled} onClick={handleClick}>
      {action.title}
    </Button>
  );
};
