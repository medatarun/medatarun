import { useContext } from "react";
import { TypeSystemContext } from "./TypeSystemContext.ts";

export const useTypeRegistry = () => {
  const typeRegistry = useContext(TypeSystemContext);
  if (!typeRegistry) {
    throw new Error("useTypeRegistry must be used within TypeSystemContext");
  }
  return { typeRegistry };
};
