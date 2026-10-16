# 📖 Mode d'Emploi & Guide d'Utilisation - TraceabilityLib

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-SDK%2021%2B-green.svg?style=flat&logo=android)](https://developer.android.com)

Ce guide pratique détaille pas à pas l'intégration et l'utilisation de **TraceabilityLib** pour le développement applicatif et la rédaction de tests unitaires et instrumentés d'observabilité (*Test-Driven Observability*).

---

## 🔗 Liens Rapides
- 🛡️ [**README.md**](README.md) : Présentation générale et diagramme d'architecture.
- 📋 [**REQUIREMENTS.md**](REQUIREMENTS.md) : Matrice de traçabilité des exigences et couverture de tests.

---

## 📑 Table des Matières

- [1. Concepts Fondamentaux](#1-concepts-fondamentaux)
- [2. Prise en Main Rapide](#2-prise-en-main-rapide)
- [3. Utilisation dans le Code de Production](#3-utilisation-dans-le-code-de-production)
  - [3.1 Journalisation à Double Canal](#31-journalisation-à-double-canal)
  - [3.2 Journalisation Défensive (`throwU` & `throwE`)](#32-journalisation-défensive-throwu--throwe)
  - [3.3 Formatage avec Repli (`LogManager.e`)](#33-formatage-avec-repli-logmanagere)
  - [3.4 Traçabilité des Exigences (`@SatisfiesRequirement`)](#34-traçabilité-des-exigences-satisfiesrequirement)
- [4. Exploitation du Journal de Vol (*Flight Recorder*)](#4-exploitation-du-journal-de-vol-flight-recorder)
  - [4.1 Récupération des Fil d'Ariane](#41-récupération-des-fil-dariane)
  - [4.2 Intégration avec un Gestionnaire de Crash (Firebase / Crashlytics)](#42-intégration-avec-un-gestionnaire-de-crash-firebase--crashlytics)
- [5. Guide pour les Tests (TDO)](#5-guide-pour-les-tests-tdo)
  - [5.1 Configuration du Harness de Test](#51-configuration-du-harness-de-test)
  - [5.2 Assertions d'Émission de Fil d'Ariane](#52-assertions-démission-de-fil-dariane)
  - [5.3 Assertion de Séquence d'Exécution](#53-assertion-de-séquence-dexécution)
  - [5.4 Vérification d'Exception et Observabilité (`assertExceptionEmitted`)](#54-vérification-dexception-et-observabilité-assertexceptionemitted)
  - [5.5 Annotation des Tests (`@CoveredRequirement`)](#55-annotation-des-tests-coveredrequirement)
  - [5.6 Tests Instrumentés Android (`androidTest` / Device & Émulateur)](#56-tests-instrumentés-android-androidtest--device--émulateur)
- [6. Bonnes Pratiques & Anti-Patterns](#6-bonnes-pratiques--anti-patterns)

---

## 1. Concepts Fondamentaux

| Concept | Description |
| :--- | :--- |
| **Dual-Channel Observability** | Séparation entre le **Canal 1** (journal de vol mémoire inconditionnel de 20 entrées) et le **Canal 2** (Logcat Android conditionnel et filtré). |
| **Marqueurs TDO** | Classification explicite des logs via `[EXPECTED]`, `[BRANCH_ALT]`, `[RECOVERY]` ou `[UNEXPECTED]`. |
| **Traceability Annotations** | Maillage `@SatisfiesRequirement("ID")` dans le code source ➔ `@CoveredRequirement("ID")` dans les tests unitaires et instrumentés. |

---

## 2. Prise en Main Rapide

### Dépendances `build.gradle.kts`
```kotlin
dependencies {
    implementation("com.parabd.lib:traceability:1.1.0")
    testImplementation("com.parabd.lib:traceability:1.1.0")
    androidTestImplementation("com.parabd.lib:traceability:1.1.0")
}
```

---

## 3. Utilisation dans le Code de Production

### 3.1 Journalisation à Double Canal

Utilisez `LogManager` à la place des appels directs à `android.util.Log` :

```kotlin
import com.parabd.lib.traceability.SatisfiesRequirement
import com.parabd.lib.traceability.utils.LogManager

@SatisfiesRequirement("6.1.1.9")
class UserAuthService {

    fun login(username: String) {
        // Log INFO (Nominal -> [EXPECTED])
        LogManager.i("AUTH", "Tentative de connexion pour l'utilisateur: $username")

        // Log WARN (Branche alternative -> [BRANCH_ALT])
        LogManager.w("AUTH", "Utilisateur $username tente une connexion sans 2FA")
    }
}
```

### 3.2 Journalisation Défensive (`throwU` & `throwE`)

Pour la programmation défensive où une erreur doit stopper l'exécution tout en étant traçée dans le journal de vol :

```kotlin
import com.parabd.lib.traceability.utils.throwU
import com.parabd.lib.traceability.utils.throwE

class PaymentProcessor {

    fun processOrder(amountDouble: Double) {
        // Anomalie structurelle -> [UNEXPECTED] + lève IllegalStateException
        if (amountDouble <= 0.0) {
            LogManager.throwU("PAYMENT", "Le montant du paiement doit être supérieur à zéro ($amountDouble)")
        }

        try {
            executeGatewayPayment(amountDouble)
        } catch (e: PaymentGatewayException) {
            // Récupération d'erreur -> [RECOVERY] + re-lève l'exception transmise
            LogManager.throwE("PAYMENT", "Échec de la passerelle de paiement", e)
        }
    }

    private fun executeGatewayPayment(amount: Double) { /* ... */ }
}
```

### 3.3 Formatage avec Repli (`LogManager.e`)

Pour journaliser une exception sans risquer d'émettre un message vide si `throwable.message` est `null` ou blanc :

```kotlin
fun loadConfig() {
    try {
        readConfigFile()
    } catch (e: Exception) {
        // Si e.message est null ou blanc, "Impossible de charger la configuration" sera utilisé comme repli.
        LogManager.e(
            tag = "CONFIG",
            prefix = "Erreur de chargement",
            throwable = e,
            fallbackMessage = "Impossible de charger le fichier de configuration"
        )
    }
}
```

### 3.4 Traçabilité des Exigences (`@SatisfiesRequirement`)

Annotez votre code de production avec l'identifiant de l'exigence fonctionnelle ou architecturale associée :

```kotlin
@SatisfiesRequirement("6.1.1.9")
fun processData() { /* ... */ }
```

---

## 4. Exploitation du Journal de Vol (*Flight Recorder*)

### 4.1 Récupération des Fil d'Ariane

Vous pouvez extraire à tout moment les 20 derniers fil d'Ariane enregistrés en mémoire :

```kotlin
val breadcrumbs: List<String> = BreadcrumbManager.getBreadcrumbs()

breadcrumbs.forEach { logEntry ->
    println(logEntry)
    // Exemple de sortie :
    // 14:23:05.112 [EXPECTED] Tentative de connexion pour l'utilisateur: admin
    // 14:23:05.115 [UNEXPECTED] Le montant du paiement doit être supérieur à zéro (-10.0)
}
```

### 4.2 Intégration avec un Gestionnaire de Crash (Firebase / Crashlytics)

Pour attacher le journal de vol à votre gestionnaire d'exceptions non gérées :

```kotlin
class CrashReporterInitializer {

    fun initGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Attachement des breadcrumbs au rapport de crash
            val flightLog = BreadcrumbManager.getBreadcrumbs().joinToString("\n")
            
            // Firebase Crashlytics exemple:
            // FirebaseCrashlytics.getInstance().log("=== TDO FLIGHT LOG ===")
            // FirebaseCrashlytics.getInstance().log(flightLog)
            // FirebaseCrashlytics.getInstance().recordException(throwable)

            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
```

---

## 5. Guide pour les Tests (TDO)

### 5.1 Configuration du Harness de Test

Dans vos classes de tests unitaires et instrumentés, réinitialisez toujours `BreadcrumbManager` et activez `forceLoggable` :

```kotlin
class PaymentProcessorTest {

    @Before
    fun setUp() {
        BreadcrumbManager.clear()
        LogManager.forceLoggable = true // Affiche les logs dans la console pendant le test
    }

    @After
    fun tearDown() {
        BreadcrumbManager.clear()
        LogManager.forceLoggable = null
    }
}
```

### 5.2 Assertions d'Émission de Fil d'Ariane

Vérifiez qu'un événement précis a été enregistré dans le journal de vol :

```kotlin
@Test
@CoveredRequirement("6.1.1.9.10")
fun testProcessOrder_NegativeAmount_EmitsUnexpectedTrace() {
    try {
        processor.processOrder(-50.0)
    } catch (_: IllegalStateException) {}

    TdoAssertions.assertBreadcrumbEmitted(
        expectedTag = TraceConstants.UNEXPECTED,
        substring = "Le montant du paiement doit être supérieur à zéro"
    )
}
```

### 5.3 Assertion de Séquence d'Exécution

Vérifiez qu'une série d'étapes s'est déroulée dans l'ordre exact :

```kotlin
@Test
@CoveredRequirement("6.1.1.9.10")
fun testWorkflow_ExecutesInCorrectOrder() {
    service.executeWorkflow()

    TdoAssertions.assertBreadcrumbSequence(
        expectedSequence = listOf(
            TraceConstants.EXPECTED to "Étape 1 : Initialisation",
            TraceConstants.EXPECTED to "Étape 2 : Traitement",
            TraceConstants.EXPECTED to "Étape 3 : Finalisation"
        ),
        strict = false // strict = true impose qu'aucun autre log intermédiaire ne soit présent
    )
}
```

### 5.4 Vérification d'Exception et Observabilité (`assertExceptionEmitted`)

Combinez en une seule assertion la levée d'exception, la vérification de son message et son traçage TDO :

```kotlin
@Test
@CoveredRequirement("6.1.1.9.10")
fun testProcessOrder_InvalidAmount_ThrowsAndEmitsTrace() {
    val exception = assertExceptionEmitted<IllegalStateException>(
        expectedSubstring = "supérieur à zéro",
        expectedMarker = TraceConstants.UNEXPECTED
    ) {
        processor.processOrder(-10.0)
    }

    assertEquals("Le montant du paiement doit être supérieur à zéro (-10.0)", exception.message)
}
```

### 5.5 Annotation des Tests (`@CoveredRequirement`)

Annotez chaque méthode de test avec l'exigence couverte afin de générer la matrice de couverture :

```kotlin
@Test
@CoveredRequirement("6.1.1.9.10")
fun testFeatureCoverage() { /* ... */ }
```

### 5.6 Tests Instrumentés Android (`androidTest` / Device & Émulateur)

Dans le cadre des tests instrumentés (UI tests avec Espresso, Compose Test, ou tests de composants Room/Services sur appareil ou émulateur), **TraceabilityLib** fonctionne de manière transparente avec l'environnement Android natif.

#### Configuration Gradle
Dans votre `build.gradle.kts` :
```kotlin
dependencies {
    androidTestImplementation("com.parabd.lib:traceability:1.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
```

#### Spécificités des Tests Instrumentés
1. **Appels Logcat réels** : Contrairement aux tests JVM locaux, `Log.isLoggable` fonctionne nativement sur l'OS Android.
2. **Journal de Vol pendant l'interaction UI** : Chaque clic, saisie de texte ou transaction de base de données déclenche de vrais événements métier qui alimentent le `BreadcrumbManager` en temps réel.
3. **Vérification de Séquence UI/ViewModel** : Vous pouvez valider qu'un scénario utilisateur complexe a généré la séquence exacte de fils d'Ariane attendue.

#### Exemple complet de Test Instrumenté Espresso
```kotlin
package com.parabd.sample

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.parabd.lib.traceability.CoveredRequirement
import com.parabd.lib.traceability.utils.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        // Purge du journal de vol avant chaque scénario UI
        BreadcrumbManager.clear()
        LogManager.forceLoggable = true // Force l'affichage dans adb logcat pendant le test instrumenté
    }

    @After
    fun tearDown() {
        BreadcrumbManager.clear()
        LogManager.forceLoggable = null
    }

    @Test
    @CoveredRequirement("6.1.1.9.10")
    fun testUserCheckoutScenario_EmitsExpectedBreadcrumbSequence() {
        // 1. Action Utilisateur sur l'IHM
        onView(withId(R.id.btn_checkout)).perform(click())

        // 2. Assertion sur le fil d'Ariane généré par l'activité et le ViewModel
        TdoAssertions.assertBreadcrumbSequence(
            expectedSequence = listOf(
                TraceConstants.EXPECTED to "Clic utilisateur sur le bouton Validation Panier",
                TraceConstants.EXPECTED to "Calcul du total de la commande",
                TraceConstants.EXPECTED to "Navigation vers l'écran de confirmation"
            ),
            strict = false
        )
    }
}
```

---

## 6. Bonnes Pratiques & Anti-Patterns

### 🟢 À Faire (Do's)
- **Toujours appeler `BreadcrumbManager.clear()`** au début (`@Before`) et à la fin (`@After`) de chaque test unitaire ou instrumenté.
- **Utiliser les extensions `throwU` et `throwE`** au lieu d'associer manuellement un log et un `throw`.
- **Systématiquement annoter** le code de production avec `@SatisfiesRequirement("ID")` et les tests avec `@CoveredRequirement("ID")`.

### 🔴 À Éviter (Donts)
- ❌ **Ne pas utiliser `android.util.Log` directement** dans le code applicatif : cela contourne le journal de vol in-memory.
- ❌ **Ne pas inclure de données sensibles** (mots de passe, tokens, données PII) dans les messages de fil d'Ariane.
