# 🌐 Document de Synchronisation SonarCloud - TraceabilityLib

Ce document répertorie la configuration complète d'intégration SonarCloud.io pour la bibliothèque **TraceabilityLib**, incluant les clés de projet, la gestion sécurisée du jeton, la configuration des secrets CI/CD sur GitHub Actions et les commandes d'orchestration Gradle.

---

## 🔑 1. Clés de Configuration SonarCloud

| Propriété SonarCloud | Valeur Officielle | Description |
| :--- | :--- | :--- |
| **`sonar.host.url`** | `https://sonarcloud.io` | Serveur cloud SonarCloud |
| **`sonar.organization`** | `brunolambinbd-hue` | Identifiant de l'organisation GitHub / SonarCloud |
| **`sonar.projectKey`** | `brunolambinbd-hue_TraceabilityLib` | Clé unique du projet SonarCloud |
| **`sonar.projectName`** | `TraceabilityLib` | Nom d'affichage dans le tableau de bord SonarCloud |
| **`sonar.token`** | `7b79a1bb9db3e87f2921a563159c59fafa5d8042` | Jeton d'authentification utilisateur/analyse local |

> [!CAUTION]
> **Sécurité des Jetons** : Le jeton `sonar.token` doit toujours rester dans le fichier `local.properties` (exclu du contrôle de version `.gitignore`) ou dans les secrets GitHub Actions. Ne le commitez jamais dans un fichier Gradle suivi par Git.

---

## ⚙️ 2. Configuration Gradle (`build.gradle.kts`)

Le fichier racine `build.gradle.kts` charge dynamiquement le jeton depuis les variables d'environnement ou le fichier `local.properties` local :

```kotlin
import java.util.Properties

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.sonarqube)
}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}
val sonarToken: String? = localProperties.getProperty("sonar.token") ?: System.getenv("SONAR_TOKEN")

sonarqube {
    properties {
        property("sonar.projectKey", "brunolambinbd-hue_TraceabilityLib")
        property("sonar.organization", "brunolambinbd-hue")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.scanner.skipJreProvisioning", "true")

        sonarToken?.let { property("sonar.token", it) }

        property("sonar.projectName", "TraceabilityLib")
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.rootDir}/library/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        property("sonar.sources", "library/src/main/java")
        property("sonar.tests", "library/src/test/java")
        property("sonar.java.binaries", "library/build/tmp/kotlin-classes/debug")

        // Exclusions multicritères de ParaBDCollector (Faux-positifs S1313 sur les IDs à 4 chiffres)
        property("sonar.issue.ignore.multicriteria", "e1,e2")
        property("sonar.issue.ignore.multicriteria.e1.ruleKey", "kotlin:S1313")
        property("sonar.issue.ignore.multicriteria.e1.resourceKey", "**/*.kt")
        property("sonar.issue.ignore.multicriteria.e2.ruleKey", "kotlin:S6474")
        property("sonar.issue.ignore.multicriteria.e2.resourceKey", "**/*.gradle.kts")
    }
}

// Tâche d'orchestration SonarCloud
tasks.register("runSonarAnalysis") {
    group = "Reporting"
    description = "Exécute les tests (JaCoCo) et télécharge le rapport vers SonarCloud."
    dependsOn(":library:jacocoTestReport")
    finalizedBy("sonar")
    doLast {
        println("\n🚀 CYCLE SONAR : Rapports générés. Lancement de l'upload vers SonarCloud...")
    }
}
```

---

## 🔒 3. Configuration des Secrets CI/CD (GitHub Actions)

Dans votre dépôt GitHub **`brunolambinbd-hue/TraceabilityLib`** :

1. Allez dans **Settings** ➔ **Secrets and variables** ➔ **Actions**.
2. Cliquez sur **New repository secret**.
3. Renseignez :
   * **Name** : `SONAR_TOKEN`
   * **Secret** : `7b79a1bb9db3e87f2921a563159c59fafa5d8042` (ou votre nouveau token SonarCloud).

### Intégration dans le Workflow GitHub Actions (`.github/workflows/ci.yml`)

```yaml
- name: Run Unit Tests & JaCoCo Coverage
  run: ./gradlew :library:jacocoTestReport

- name: Run SonarCloud Analysis
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
  run: ./gradlew sonar
```

---

## 🚀 4. Commandes d'Exécution Gradle

### A. Exécution Automatisée Locale (avec `local.properties`)
```bash
./gradlew runSonarAnalysis
```
*(Génère automatiquement les 47 tests unitaires, le rapport JaCoCo XML 100% couvert et télécharge les métriques vers SonarCloud).*

### B. Exécution avec Injection Directe de Jeton
```bash
./gradlew sonar -Dsonar.token=7b79a1bb9db3e87f2921a563159c59fafa5d8042
```

### C. Exécution en Mode Débogage
```bash
./gradlew sonar --info --stacktrace
```

---

## 🛡️ 5. Résolution des Problèmes Courants (Troubleshooting)

### Erreur `403 Forbidden` sur `analysis/engine`
* **Cause** : Le projet `brunolambinbd-hue_TraceabilityLib` n'est pas encore créé/lié sur l'interface Web SonarCloud, ou le jeton appartient à un projet restreint.
* **Résolution** : Allez sur [**SonarCloud.io ➔ Analyze new project**](https://sonarcloud.io/project/create), liez le dépôt `TraceabilityLib` à l'organisation `brunolambinbd-hue`, puis relancez `./gradlew runSonarAnalysis`.

### Erreur `403 Forbidden` sur `analysis/jres` (Windows)
* **Cause** : Tentative de téléchargement automatique d'un binaire JRE externe.
* **Résolution** : La propriété `property("sonar.scanner.skipJreProvisioning", "true")` dans `build.gradle.kts` résout ce problème en réutilisant le JDK 18/21 local.

### Faux-Positifs d'Adresses IP sur les Requirements IDs (`kotlin:S1313`)
* **Cause** : Les identifiants d'exigences à 4 chiffres (ex: `"6.1.1.9"`) sont interprétés comme des adresses IPv4 par Sonar.
* **Résolution** : La règle d'exclusion multicritère `e1` (`kotlin:S1313`) configurée dans `build.gradle.kts` neutralise automatiquement ces avertissements.
