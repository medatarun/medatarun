import type {PropsWithChildren} from "react";
import {tokens} from "@fluentui/react-components";

export function SectionTable({children}: PropsWithChildren) {
  return <div style={{
    marginTop: 0,
    backgroundColor: tokens.colorNeutralBackground1,
    padding: 0,
    borderRadius: tokens.borderRadiusMedium,
    marginBottom: tokens.spacingVerticalM,
  }}>{children}</div>
}