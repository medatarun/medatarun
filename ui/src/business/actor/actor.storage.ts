import { executeActionJson } from "@/business/action_runner";
import type { WhoAmIRespDto } from "@/business/actor/actor.dto.ts";
import { useQuery } from "@tanstack/react-query";

async function whoami() {
  return executeActionJson<WhoAmIRespDto>("auth", "whoami", {})
}

export const useWhoami = (issuer: string | null, subject: string | null) => {
  return useQuery({
    queryKey: ["whoami", issuer, subject],
    queryFn: whoami,
  });
};