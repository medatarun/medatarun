import {createContext, useContext} from "react";
import {Model} from "../../business";

export const ModelContext = createContext<Model | undefined>(undefined)

export function useModelContext(): Model {
  const context = useContext(ModelContext)
  if (context == null) throw Error("No ModelContext available")
  return context
}