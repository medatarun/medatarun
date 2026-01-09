import {trim} from "lodash-es";

/**
 *
 * @param value valeur a renvoyer si présente
 * @param defaultValue  valeur par défaut
 * @returns value si non nut et not undefined et pas vide, sinon defaultValue
 */
export function getOrDefault(value: string | null | undefined, defaultValue: string) {
  if (value === null) return defaultValue;
  if (value === undefined) return defaultValue;
  if (value === "") return defaultValue;
  const trimmed = trim(value);
  if (trimmed === "") return defaultValue;
  return value;
}
