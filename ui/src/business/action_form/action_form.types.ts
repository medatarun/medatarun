export type FormDataType = Record<string, unknown>;
export type FormFieldType = {
  key: string
  title: string
  description: string | null
  optional: boolean,
  type: string
  order: number
  readonly: boolean
  visible: boolean
}