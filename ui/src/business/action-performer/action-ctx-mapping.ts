import type { ActionDisplayedSubject } from "./action-ctx-displayed-subject.ts";
import type { ActionRequest } from "./action-request.ts";
import type { ActionKey } from "@/business/action_registry";
import type { ActionCtx } from "./action-ctx.ts";

/**
 * Implementation of an ActionCtx that provide a simple way to map data to
 * action parameters.
 *
 * This is what most components will use to prefill actions with data and
 * adjust the behavior of the action and post-action
 *
 * To create that, you provide a serie of mappings that matches action
 * parameters to the component data.
 *
 * For example, the "value" action parameter from the action
 * "model_update_name", shall come from the component displayed model's `name`,
 * should not be readonly and visible
 *
 * Another example, the `modelRef` parameter of the same action should not
 * be visible and pre-populated with the model UUID `id:<id>`
 *
 * Doing that, most action parameters and post-action behaviors should
 * be resolved dynamically.
 */
export class ActionCtxMapping implements ActionCtx {
  modelMapping: Map<string, ActionCtxMappingParam>;
  displayedSubject: ActionDisplayedSubject;
  private mappings: ActionCtxMappingParam[];
  constructor(
    mappings: ActionCtxMappingParam[],
    displayedSubject: ActionDisplayedSubject,
  ) {
    this.displayedSubject = displayedSubject;
    this.mappings = mappings;
    this.modelMapping = new Map(mappings.map((it) => [it.actionParamKey, it]));
  }

  getDefaultValue = (paramKey: string, req: ActionRequest) => {
    const found = this.mappings.find((m) =>
      match(req.actionGroupKey, req.actionKey, paramKey, m),
    );
    if (found) {
      return found.defaultValue();
    }
    return undefined;
  };

  isPresent = (key: string): boolean => {
    return this.modelMapping.get(key) !== undefined;
  };
  isReadonly = (key: string) => {
    return this.modelMapping.get(key)?.readonly ?? false;
  };
  isVisible = (key: string) => {
    return this.modelMapping.get(key)?.visible ?? true;
  };
}

export interface ActionCtxMappingParam {
  actionGroupKey?: RegExp | string | undefined;
  actionKey?: RegExp | ActionKey | undefined;
  actionParamKey: string;
  defaultValue: () => unknown;
  readonly?: boolean;
  visible?: boolean;
}

const matchValue = (
  reference: string,
  value: RegExp | string | undefined,
): boolean => {
  if (value == undefined) return true;
  if (typeof value == "string") return value === reference;
  if (value instanceof RegExp) return value.test(reference);
  return false;
};

const match = (
  actionGroupKey: string,
  actionKey: string,
  actionParamKey: string,
  mapping: ActionCtxMappingParam,
): boolean => {
  const matchGroup = matchValue(actionGroupKey, mapping.actionGroupKey);
  if (!matchGroup) return false;
  const matchAction = matchValue(actionKey, mapping.actionKey);
  if (!matchAction) return false;
  return actionParamKey === mapping.actionParamKey;
};
