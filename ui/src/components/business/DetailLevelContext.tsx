import {createContext, type PropsWithChildren, useContext, useState} from "react";

export type DetailLevel = "business" | "tech"
type DetailLevelContextValue = {
  level: DetailLevel;
  setLevel: (level: DetailLevel) => void;
};

const DetailLevelContext = createContext<DetailLevelContextValue | null>(null)

export const DetailLevelProvider = ({children}: PropsWithChildren) => {
  const [level, setLevel] = useState<DetailLevel>("business")
  return (
    <DetailLevelContext.Provider value={{level, setLevel}}>{children}</DetailLevelContext.Provider>
  )
}

export const useDetailLevelContext = () => {
  const ctx = useContext(DetailLevelContext)
  if (!ctx) throw new Error("DetailLevelCtx not provided");
  return {
    isDetailLevelTech: ctx.level === "tech",
    toggle: () => ctx.level === "tech" ? ctx.setLevel("business") : ctx.setLevel("tech")
  }
}