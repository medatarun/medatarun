import type {PropsWithChildren} from "react";
import {LargeTitle} from "@fluentui/react-components";

export function ViewTitle({children}: PropsWithChildren) {
  return <LargeTitle>{children}</LargeTitle>;
}