import { type PropsWithChildren, useState } from "react";
import { type DetailLevel, DetailLevelContext } from "./DetailLevel.context.ts";

export const DetailLevelProvider = ({ children }: PropsWithChildren) => {
  const defaultValueStorage = localStorage.getItem("detailLevel");
  const defaultValue = defaultValueStorage === "tech" ? "tech" : "business";

  const [level, setLevel] = useState<DetailLevel>(defaultValue);
  const handleLevelChange = (value: DetailLevel) => {
    localStorage.setItem("detailLevel", value);
    setLevel(value);
  };
  return (
    <DetailLevelContext.Provider value={{ level, setLevel: handleLevelChange }}>
      {children}
    </DetailLevelContext.Provider>
  );
};
