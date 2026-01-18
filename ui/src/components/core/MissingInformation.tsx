import type {PropsWithChildren} from "react";
import {Text, tokens} from "@fluentui/react-components";

export function MissingInformation({children}: PropsWithChildren) {
  return <Text italic style={{color: tokens.colorNeutralStroke1}}>{children}</Text>
}