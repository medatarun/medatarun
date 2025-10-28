package io.medatarun.ext.modeljson

import org.intellij.lang.annotations.Language

@Language("json")
internal val sampleModelJson = """{
  "id": "example",
  "version": "1.0.0",
  "types": [
    { "id":  "String", "name": "Simple text", "description": "Simple text max 255 chars" },
    { "id":  "Markdown" }
  ],
  "entities": [
    {
      "id": "contact",
      "name": "Contact",
      "identifierAttribute": "name",
      "attributes": [
        {
          "id": "name",
          "name": "Name",
          "type": "String"
        },
        {
          "id": "role",
          "name": "Role",
          "type": "String"
        },
        {
          "id": "location",
          "name": "Location",
          "type": "String",
          "optional": true
        },
        {
          "id": "profile_url",
          "name": "Profile URL",
          "type": "String"
        },
        {
          "id": "capture_date",
          "name": "Capture date",
          "type": "LocalDate"
        },
        {
          "id": "informations",
          "name": "Informations",
          "type": "Markdown",
          "optional": true
        }
      ]
    },
    {
      "id": "company",
      "name": {
        "fr": "Entreprise",
        "en": "Company"
      },
      "identifierAttribute": "name",
      "attributes": [
        {
          "id": "name",
          "name": "Name",
          "type": "String"
        },
        {
          "id": "profile_url",
          "name": "Profile URL",
          "description": "Website URL",
          "type": "String",
          "optional": true
        },
        {
          "id": "informations",
          "name": "Informations",
          "description": {
            "fr": "La description est au format Markdown et doit provenir de leur site internet !"
          },
          "type": "Markdown",
          "optional": true
        }
      ]
    }
  ],
  "relationships": []
}"""