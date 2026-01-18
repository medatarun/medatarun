import type {PropsWithChildren} from "react";
import {tokens} from "@fluentui/react-components";

export function ContainedHumanReadable({children}: PropsWithChildren) {
  return <div style={{margin: "auto", width: "80rem"}}>{children}</div>
}

export function ContainedFixed({children}: PropsWithChildren) {
  return children
}

export function ContainedMixedScrolling({children}: PropsWithChildren) {
  return <div style={{
    display: "flex",
    flexDirection: "column",
    height: "100%",
    overflow: "hidden"
  }}>{children}</div>
}
export function ContainedHeader({children}: PropsWithChildren) {
  return <div style={{
    marginTop: tokens.spacingVerticalM,
  }}>{children}</div>
}

export function ContainedScrollable({children}: PropsWithChildren) {
  return <div style={{flexGrow: 1, overflowY: "auto"}}>{children}</div>
}

