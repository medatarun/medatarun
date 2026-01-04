import {useQuery} from "@tanstack/react-query";
import {fetchModel, fetchModelSummaries} from "./model.api.ts";

export function useModelSummaries() {
  return useQuery({
    queryKey: ["model_summaries"],
    queryFn: fetchModelSummaries
  })
}

export function useModel(modelKey: string) {
  return useQuery({
    queryKey: ["model", modelKey],
    queryFn: ()=>fetchModel(modelKey)
  })
}