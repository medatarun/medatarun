/**
 * Abstract type description matching the platform types found in the type system
 */
export interface TypeDescriptorDto {
  /**
   * Identifier of the type as a multiplatform type (in the backend, called "equivMultiplatform")
   */
  id: string;
  /**
   * Expected type when serialized/deserialized from JSON
   */
  equivJson: "string" | "boolean" | "number" | "object" | "array";
  /**
   * Textual description of the type
   */
  description: string;
}
