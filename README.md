# TraceabilityLib

## Description
Bibliothèque utilitaire légère fournissant les annotations standard pour la traçabilité des exigences (Requirements Traceability) au sein de l'écosystème ParaBD.

## Fonctionnalités
- `@SatisfiesRequirement(id)` : Pour marquer le code de production implémentant une exigence.
- `@CoveredRequirement(id)` : Pour lier les tests unitaires et instrumentés aux exigences.

## Installation
1. Publier localement : `./gradlew publishToMavenLocal`
2. Dans votre projet, ajoutez `mavenLocal()` à vos dépôts.
3. Ajoutez la dépendance :
```kotlin
implementation("com.example.lib:traceability:1.0.0")
```
