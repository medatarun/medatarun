import { ActionPerformerInstance } from "./action-performer-factory.ts";

/**
 * Executes an action that, if it returns something, will be a JSON result
 */
export const executeActionJson = ActionPerformerInstance.executeJson;
/**
 * Executes an action that may return any format (text or JSON)
 */
export const executeActionAny = ActionPerformerInstance.executeAny;
