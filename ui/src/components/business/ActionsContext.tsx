import type {ActionRegistry} from "../../business";
import {createContext, useContext} from "react";

export const ActionsContext = createContext<ActionRegistry|undefined>(undefined);
export const useActionRegistry = (): ActionRegistry => {
  const actions = useContext(ActionsContext)
  if (!actions) throw new Error("useActions must be used within ActionsContext");
  return actions;
}