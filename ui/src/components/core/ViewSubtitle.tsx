import {type FluentIcon} from "@fluentui/react-icons";
import {Title3} from "@fluentui/react-components";
import type {PropsWithChildren} from "react";

export function ViewSubtitle({icon, children}: { icon?: FluentIcon } & PropsWithChildren) {
  const Icon = icon
  return <div><Title3>
    {Icon && <span style={{marginRight: "0.2em"}}><Icon fontSize={18} style={{verticalAlign: "baseline"}}/></span>}
    {children}
  </Title3></div>

}