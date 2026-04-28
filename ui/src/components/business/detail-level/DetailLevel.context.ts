import { createContext } from "react";

export type DetailLevel = "business" | "tech";
type DetailLevelContextValue = {
  level: DetailLevel;
  setLevel: (level: DetailLevel) => void;
};
export const DetailLevelContext = createContext<DetailLevelContextValue | null>(
  null,
);
