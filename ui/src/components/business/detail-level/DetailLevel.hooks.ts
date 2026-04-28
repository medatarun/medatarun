import { useContext } from "react";
import { DetailLevelContext } from "./DetailLevel.context.ts";

export const useDetailLevelContext = () => {
  const ctx = useContext(DetailLevelContext);
  if (!ctx) throw new Error("DetailLevelCtx not provided");
  return {
    isDetailLevelTech: ctx.level === "tech",
    toggle: () =>
      ctx.level === "tech" ? ctx.setLevel("business") : ctx.setLevel("tech"),
  };
};
