import type {PropsWithChildren} from "react";
import {tokens} from "@fluentui/react-components";

export function SectionCards  ({children}:PropsWithChildren)  {
  return <div style={{
    marginTop: 0,
    padding: 0,
    borderRadius: tokens.borderRadiusMedium,
    marginBottom: tokens.spacingVerticalM,
  }}>{children}</div>
}