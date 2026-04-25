import type { PropsWithChildren, ReactNode } from "react";
import { Text, tokens } from "@fluentui/react-components";
import {
  type ActionCtx,
  createActionCtxVoid,
} from "@/business/action-performer";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";
import type { ActionKey } from "@/business/action_registry/actionRegistry.dictionnary.ts";
import { useActionRegistry } from "@/components/business/actions";

export function SectionTitle({
  icon,
  actions,
  actionCtx,
  children,
}: {
  icon: ReactNode;
  actions?: ActionKey[];
  actionCtx?: ActionCtx;
} & PropsWithChildren) {
  const actionRegistry = useActionRegistry();
  const actionDescriptors = actions
    ? actionRegistry.findActionDescriptors(actions)
    : [];
  const actionCtxSafe = actionCtx ? actionCtx : createActionCtxVoid();

  return (
    <div
      style={{
        display: "flex",
        columnGap: tokens.spacingHorizontalS,
        alignItems: "center",
        justifyContent: "start",
        borderBottom: "1px solid #ccc",
        paddingBottom: tokens.spacingVerticalS,
        paddingRight: tokens.spacingVerticalS,
      }}
    >
      <div
        style={{
          flex: 1,
          display: "flex",
          columnGap: tokens.spacingVerticalS,
          alignItems: "center",
        }}
      >
        {icon && (
          <div>
            <Text size={500} weight="semibold" style={{ display: "flex" }}>
              {icon}
            </Text>
          </div>
        )}
        <div>
          <Text size={400} weight="semibold">
            {children}
          </Text>
        </div>
      </div>
      <div>
        <ActionMenuButton
          actionCtx={actionCtxSafe}
          itemActions={actionDescriptors}
        />
      </div>
    </div>
  );
}
