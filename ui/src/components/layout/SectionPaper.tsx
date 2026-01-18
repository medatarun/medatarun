import type {PropsWithChildren} from "react";
import {tokens} from "@fluentui/react-components";

export function SectionPaper({children, topspacing = "M"}:{topspacing?:"M"|"XXXL"} &PropsWithChildren) {
  const topspacingToken = topspacing == "XXXL" ? tokens.spacingVerticalXXXL : tokens.spacingVerticalM
  return <div style={{
    backgroundColor: tokens.colorNeutralBackground1,
    padding: tokens.spacingVerticalM,
    borderRadius: tokens.borderRadiusMedium,
    marginTop: topspacingToken,
    marginBottom: tokens.spacingVerticalM,
  }}>{children}</div>

}