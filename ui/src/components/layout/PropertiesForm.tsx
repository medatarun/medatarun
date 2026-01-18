import type {PropsWithChildren} from "react";
import {tokens} from "@fluentui/react-components";

export function PropertiesForm({children}: PropsWithChildren) {
  return <div style={{
    display: "grid",
    gridTemplateColumns: "min-content auto",
    columnGap: tokens.spacingVerticalM,
    rowGap: tokens.spacingVerticalM,
    alignItems: "baseline"
  }}>{children}
  </div>
}