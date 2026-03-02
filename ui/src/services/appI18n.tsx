import { I18nServiceInstance, useI18n } from "@seij/common-ui";
import { type Messages } from "../locales/Messages";
import { messages as fr } from "../locales/fr";
import { messages as en } from "../locales/en";

const namespace = "medatarun";
I18nServiceInstance.registerNamespace(namespace, { fr: fr, en: en });

export function useAppI18n() {
  const i18n = useI18n();
  return {
    t: (key: keyof Messages, values?: Record<string, unknown>) => i18n.t(namespace + ":" + key, values),
  };
}
