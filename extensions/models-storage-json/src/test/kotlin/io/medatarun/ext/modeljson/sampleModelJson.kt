package io.medatarun.ext.modeljson

import org.intellij.lang.annotations.Language

@Language("json")
internal val sampleModelJson = $$"""{
  "id": "019be5cc-12e7-781c-bfdc-41ed61702cb9",
  "key": "example",
  "$schema": "https://raw.githubusercontent.com/medatarun/medatarun/main/schemas/medatarun-model-1.1.json",
  "version": "1.0.0",
  "hashtags": [
    "a",
    "b",
    "c",
    "d"
  ],
  "types": [
    {
      "id": "019be5cd-2ce6-7c51-b4ec-43aa4517b56b",
      "key": "String",
      "name": "Simple text",
      "description": "Simple text max 255 chars"
    },
    {
      "id": "019be5cd-499b-7663-ac33-e28c419a5bba",
      "key": "Markdown"
    }
  ],
  "entities": [
    {
      "id": "019be5cc-6207-7755-b607-f4a539252733",
      "key": "contact",
      "name": "Contact",
      "identifierAttribute": "name",
      "hashtags": [
        "e1",
        "e2"
      ],
      "attributes": [
        {
          "id": "019be5cd-e3e3-715a-9de9-4aa368a2401c",
          "key": "name",
          "name": "Name",
          "type": "String",
          "hashtags": [
            "private"
          ]
        },
        {
          "id": "019be5ce-0f33-78ce-8f32-e24311b8658d",
          "key": "role",
          "name": "Role",
          "type": "String",
          "hashtags": [
            "private",
            "rgpd"
          ]
        },
        {
          "id": "019be5ce-2f96-790c-9bee-359d164007c6",
          "key": "location",
          "name": "Location",
          "type": "String",
          "optional": true
        },
        {
          "id": "019be5ce-497e-7559-b98d-c991cd3ee99d",
          "key": "profile_url",
          "name": "Profile URL",
          "type": "String"
        },
        {
          "id": "019be5ce-8553-7824-994a-13ad098b3d49",
          "key": "capture_date",
          "name": "Capture date",
          "type": "LocalDate"
        },
        {
          "id": "019be5ce-66e4-7720-9d78-d2ba1bea1abe",
          "key": "informations",
          "name": "Informations",
          "type": "Markdown",
          "optional": true
        }
      ]
    },
    {
      "id": "019be5ce-d771-7c12-8c97-ae547f106387",
      "key": "company",
      "name": {
        "fr": "Entreprise",
        "en": "Company"
      },
      "identifierAttribute": "name",
      "hashtags": [
        "e3",
        "e4"
      ],
      "attributes": [
        {
          "id": "019be5cf-142c-737d-a1c2-3434cdb13912",
          "key": "name",
          "name": "Name",
          "type": "String"
        },
        {
          "id": "019be5cf-39e9-7716-a229-8432d1b425c3",
          "key": "profile_url",
          "name": "Profile URL",
          "description": "Website URL",
          "type": "String",
          "optional": true
        },
        {
          "id": "019be5cf-5994-795b-8a79-59045b0a8d8d",
          "key": "informations",
          "name": "Informations",
          "description": {
            "fr": "La description est au format Markdown et doit provenir de leur site internet !"
          },
          "type": "Markdown",
          "optional": true
        }
      ]
    }
  ]
}"""