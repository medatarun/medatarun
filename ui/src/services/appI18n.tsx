import { I18nServiceInstance, useI18n } from "@seij/common-ui";
import { type Messages } from "../locales/Messages";
import { messages as fr } from "../locales/fr";
import { messages as en } from "../locales/en";

const namespace = "medatarun";
I18nServiceInstance.registerNamespace(namespace, { fr: fr, en: en });
export type AppMessageKey = keyof Messages;

/**
 * Translates an application message outside React hooks.
 *
 * Use this only in places where hooks cannot be used, for example class
 * components. This helper must stay exceptional so the standard path for UI
 * code remains useAppI18n().
 *
 * @param key The typed application message key to translate.
 * @param values Optional interpolation values passed to i18n.
 * @returns The translated string for the current locale.
 */
export function appT(key: AppMessageKey, values?: Record<string, unknown>) {
  return I18nServiceInstance.t(namespace + ":" + key, values);
}

/**
 * Returns the standard translation helper for React function components.
 *
 * This is the default way to access application messages in UI code. It keeps
 * translations typed through keyof Messages and follows the normal React hook
 * model.
 *
 * Usage:
 * const { t } = useAppI18n();
 * t("someMessageKey");
 * t("someMessageKey", { value: "example" });
 *
 * @returns An object exposing the typed translation function `t`.
 */
export function useAppI18n() {
  const i18n = useI18n();
  return {
    t: (key: AppMessageKey, values?: Record<string, unknown>) =>
      i18n.t(namespace + ":" + key, values),
  };
}
