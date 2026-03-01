import type {ActionPerformerRequestParam} from "../../components/business/actions/ActionPerformer.tsx";

export const refid = (id: string):  ActionPerformerRequestParam => ({value: "id:" +id, readonly: true})