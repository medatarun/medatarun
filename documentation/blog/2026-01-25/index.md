---
slug: 2026-01-25-addressing-objects
title: Adressing Objects in Medatarun
authors: [ sebastienjust ]
tags: [ announcements ]
---

# Adressing Objects in Medatarun

Object addressing in APIs is a user concern, not just a "purely technical" question. It directly affects ease of use (DX) and integration complexity, and therefore build and maintenance costs: our costs and our users' costs.

<!-- truncate -->

Using IDs or business keys is not a minor detail. Once the choice is made, it commits clients for the long term, often well after the API has gone into production. 

The real issue is not choosing one over the other, but avoiding locking the API into that choice.

On Medatarun, I wanted to treat that as a contract and product architecture concern, not as a technical detail. Due to the nature of Medatarun as an integration tool, this question was there from the beginning: how can I make writing scripts and tools easier, with data coming from every part of the Information System.

I took the time to write down the reasoning here:

👉 [Addressing objects in Medatarun](../../docs/resources/arch-api-addressing-reference)

These decisions are invisible at launch.
They only become noticeable when they haven’t been made.

<!--

L’adressage des objets dans les API est un sujet client, pas "que technique". Cela conditionne la facilité d’usage et d’intégration, donc directement les coûts de conception et de maintenance : les nôtres, les leurs.

Utiliser des ids ou des clés métier n’est pas un détail. Ce choix engage les clients dans la durée, souvent bien après que l’API a été mise en production.

Le vrai sujet n’est pourtant pas de choisir entre les deux, mais de ne pas enfermer l’API dans ce choix.

Sur Medatarun, j’ai posé ce problème comme un sujet de contrat et d’architecture produit, pas comme un détail technique.

J’ai pris le temps d’écrire le raisonnement ici :


Ces décisions sont invisibles au lancement.
On ne les remarque que lorsqu’elles n’ont pas été prises.

-->