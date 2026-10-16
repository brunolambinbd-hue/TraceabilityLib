# 📋 Matrice des Exigences & Traçabilité - TraceabilityLib

[![Matrix Status](https://img.shields.io/badge/Requirements-100%25%20Covered-brightgreen.svg)](#)
[![Tests Status](https://img.shields.io/badge/Unit%20Tests-47%20Passed-blue.svg)](#)

Ce document répertorie les exigences fonctionnelles et architecturales satisfaites par le code de production et vérifiées par la suite de tests unitaires de **TraceabilityLib**.

---

## 🔗 Liens Rapides
- 🛡️ [**README.md**](README.md) : Présentation générale et diagramme d'architecture.
- 📖 [**USER_GUIDE.md**](USER_GUIDE.md) : Mode d'emploi pas à pas, exemples de code et tests unitaires/instrumentés.

---

## 📐 Convention de Structure des Identifiants d'Exigences (Format ID)

Les identifiants d'exigences suivent la nomenclature hiérarchique officielle du système **ParaBD** :

* **Exigences de Production (`@SatisfiesRequirement`)** :
  - **4 Nombres** (ex: `6.1.1.9`, `6.1.2.9`, `6.3.2.2`) : Exigence de niveau composant ou d'architecture.
  - **5 Nombres** (ex: `6.1.2.9.1`) : Sous-exigence spécifique ou règle de gestion détaillée.

* **Exigences de Test & Recette (`@CoveredRequirement`)** :
  - **5 Nombres** (ex: `6.1.1.9.10`, `6.3.2.9.10`) : Exigence originale complétée par le suffixe `.10` identifiant le scénario de test automatisé TDO.

---

## 🌐 Schéma de Traçabilité Exigence ➔ Implémentation ➔ Test

```mermaid
graph LR
    subgraph Requirements ["📋 Exigences (Chapitre 6)"]
        R_6119["6.1.1.9 / 6.3.2.9\nDual-Channel Logging"]
        R_6129["6.1.2.9 / 6.1.2.9.1\nFlight Recorder Buffer"]
        R_6322["6.3.2.2\nTDO Markers"]
        R_611910["6.1.1.9.10 / 6.3.2.9.10\nTDO Assertions"]
    end

    subgraph Production ["📦 Code de Production (@SatisfiesRequirement)"]
        LogMgr["LogManager & LogManagerExt"]
        BreadcrumbMgr["BreadcrumbManager"]
        TraceConst["TraceConstants"]
    end

    subgraph Tests ["🧪 Tests Unitaires (@CoveredRequirement)"]
        LogMgrTest["LogManagerTest & LogManagerExtTest"]
        BreadcrumbTest["BreadcrumbManagerTest"]
        AssertionsTest["TdoAssertionsTest & ExceptionTestUtilsTest"]
    end

    R_6119 --> LogMgr --> LogMgrTest
    R_6129 --> BreadcrumbMgr --> BreadcrumbTest
    R_6322 --> TraceConst
    R_611910 --> AssertionsTest

    classDef req fill:#2b5c8f,stroke:#fff,color:#fff;
    classDef prod fill:#1e3a5f,stroke:#3b82f6,color:#fff;
    classDef test fill:#0f5132,stroke:#198754,color:#fff;
    class R_6119,R_6129,R_6322,R_611910 req;
    class LogMgr,BreadcrumbMgr,TraceConst prod;
    class LogMgrTest,BreadcrumbTest,AssertionsTest test;
```

---

## 📊 Tableau Détaillé des Exigences

| ID Exigence | Format / Niveau | Composant Implémenté | Description Fonctionnelle / Architecturale | Statut de Couverture |
| :--- | :---: | :--- | :--- | :---: |
| **`6.1.1.9`** | 4 Nombres | `LogManager`, `LogManagerExt` | **API d'Observabilité Centralisée à Double Canal** : Émission inconditionnelle des breadcrumbs pour le journal de vol + filtrage Logcat conditionnel via `Log.isLoggable`. | `✅ PASS` |
| **`6.1.2.9`** | 4 Nombres | `BreadcrumbManager` | **Tampon Circulaire In-Memory (Flight Recorder)** : Stockage thread-safe des $N$ derniers événements qualifiés avec horodatage (`HH:mm:ss.SSS`). | `✅ PASS` |
| **`6.1.2.9.1`**| 5 Nombres | `BreadcrumbManager` | **Rotation & Purge du Tampon** : Rotation automatique (maintien des 20 derniers logs) et méthode de purge `clear()` pour les tests. | `✅ PASS` |
| **`6.3.2.2`** | 4 Nombres | `TraceConstants` | **Nomenclature Technique TDO** : Définition des constantes de marqueurs d'exécution (`[EXPECTED]`, `[BRANCH_ALT]`, `[RECOVERY]`, `[UNEXPECTED]`). | `✅ PASS` |
| **`6.3.2.9`** | 4 Nombres | `LogManager`, `LogManagerExt`, `TdoAssertions` | **API de Journalisation Défensive & Contrôle de Séquence** : Extensions `throwU`, `throwE`, `e` avec repli et assertions d'observabilité. | `✅ PASS` |
| **`6.1.1.9.10`**| 5 Nombres | `TdoAssertions`, `ExceptionTestUtils` | **Assertions TDO pour Tests Unitaires** : Vérification unifiée de la présence des fils d'Ariane et des séquences d'exécution. | `✅ PASS` |
| **`6.3.2.9.10`**| 5 Nombres | `TdoAssertions`, `ExceptionTestUtils` | **Observabilité des Exceptions** : Vérification combinée type/message d'exception + trace d'observabilité dans le journal de vol. | `✅ PASS` |

---

## 🏷️ Annotations de Traçabilité

### `@SatisfiesRequirement(id: String)`
Renseigne l'exigence fonctionnelle ou architecturale satisfaite par un élément de code de production.
* **Format ID** : `x.x.x.x` (4 nombres) ou `x.x.x.x.x` (5 nombres)
* **Cibles** : `CLASS`, `FUNCTION`, `PROPERTY`, `CONSTRUCTOR`
* **Rétention** : `SOURCE`

### `@CoveredRequirement(id: String)`
Renseigne l'exigence fonctionnelle vérifiée et couverte par une suite de tests unitaires ou d'intégration.
* **Format ID** : `x.x.x.x.10` (5 nombres avec le suffixe `.10` de test)
* **Cibles** : `CLASS`, `FUNCTION`, `PROPERTY`, `CONSTRUCTOR`
* **Rétention** : `SOURCE`

---

## 🧪 Suites de Tests Unitaires Associées

| Classe de Test | Exigences Couvertes (5 Nombres) | Nombre de Tests | Description de la Couverture |
| :--- | :--- | :---: | :--- |
| **`BreadcrumbManagerTest`** | `6.1.2.9.1` | 3 | Valide le stockage, la rotation du tampon circulaire (max 20) et la réinitialisation `clear()`. |
| **`LogManagerTest`** | `6.1.1.9.10`, `6.3.2.9.10` | 11 | Valide l'émission inconditionnelle des breadcrumbs et le contrôle par flag `forceLoggable`. |
| **`LogManagerExtTest`** | `6.1.1.9.10`, `6.3.2.9.10` | 11 | Valide les extensions `throwU`, `throwE` et `e` avec préfixe/repli. |
| **`LogManagerRobolectricTest`** | `6.1.1.9.10`, `6.3.2.9.10` | 4 | Valide le comportement sous ombres framework Android avec `ShadowLog`. |
| **`TdoAssertionsTest`** | `6.1.1.9.10`, `6.3.2.9.10` | 10 | Valide les assertions d'émission de breadcrumbs (Cas A, B, C) et le contrôle de séquence. |
| **`ExceptionTestUtilsTest`** | `6.1.1.9.10`, `6.3.2.9.10` | 5 | Valide la vérification unifiée type exception + message + trace TDO d'observabilité. |
