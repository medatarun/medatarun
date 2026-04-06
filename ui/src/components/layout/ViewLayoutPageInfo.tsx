import type { PropsWithChildren } from "react";
import { MessageBar, MessageBarBody } from "@fluentui/react-components";

export function ViewLayoutPageInfo(props:PropsWithChildren) {
  return (
    <p>
      <MessageBar intent={"info"} layout="multiline">
        <MessageBarBody>{props.children}</MessageBarBody>
      </MessageBar>
    </p>
  );
}