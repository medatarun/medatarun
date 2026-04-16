import type { ActionPerformerRequestParam } from "@/components/business/actions";

export const refid = (id: string): ActionPerformerRequestParam => ({
  value: "id:" + id,
  readonly: true,
  visible: false,
});
