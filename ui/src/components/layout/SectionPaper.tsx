import type {PropsWithChildren} from "react";
import {tokens} from "@fluentui/react-components";

export function SectionPaper({children, topspacing = "M", nopadding = false}:{topspacing?:"M"|"XXXL", nopadding?: boolean} &PropsWithChildren) {
  const topspacingToken = topspacing == "XXXL" ? tokens.spacingVerticalXXXL : tokens.spacingVerticalM
  return <div style={{
    backgroundColor: tokens.colorNeutralBackground1,
    padding: nopadding ? 0 : tokens.spacingVerticalM,
    borderRadius: tokens.borderRadiusMedium,
    marginTop: topspacingToken,
    marginBottom: tokens.spacingVerticalM,
  }}>{children}</div>

}