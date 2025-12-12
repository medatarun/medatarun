import type {PropsWithChildren} from "react";

export function TabPanel(props:PropsWithChildren) {
  return <div style={{padding:"1em"}}>{props.children}</div>
}