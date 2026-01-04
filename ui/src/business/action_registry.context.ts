import {createContext} from "react";
import type {ActionRegistry} from "./action_registry.biz.tsx";

export const ActionsContext = createContext<ActionRegistry|undefined>(undefined);