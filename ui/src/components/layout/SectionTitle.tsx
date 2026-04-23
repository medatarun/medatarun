import type { PropsWithChildren, ReactNode } from "react";
import { useActionRegistry } from "@/business/action_registry";
import { Text, tokens } from "@fluentui/react-components";
import {
  type ActionCtx,
  createActionCtxVoid,
} from "@/business/action-performer";
import { ActionMenuButton } from "@/components/business/actions/ActionMenuButton.tsx";
import type { ActionKey } from "@/business/action_registry/actionRegistry.dictionnary.ts";

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
          actionCtx={actionCtxSafe}
          itemActions={actionDescriptors}
        />
      </div>
    </div>
  );
}
