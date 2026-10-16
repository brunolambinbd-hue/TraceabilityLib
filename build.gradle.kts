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

        // Exclusions multicritères de ParaBDCollector pour ignorer les faux-positifs S1313 (IPs/Requirement IDs)
        property("sonar.issue.ignore.multicriteria", "e1,e2")

        // e1 : Ignorer les faux-positifs d'adresses IP (S1313) sur les IDs à 4 chiffres des fichiers Kotlin
        property("sonar.issue.ignore.multicriteria.e1.ruleKey", "kotlin:S1313")
        property("sonar.issue.ignore.multicriteria.e1.resourceKey", "**/*.kt")

        // e2 : Ignorer S6474 sur les fichiers Gradle
        property("sonar.issue.ignore.multicriteria.e2.ruleKey", "kotlin:S6474")
        property("sonar.issue.ignore.multicriteria.e2.resourceKey", "**/*.gradle.kts")
    }
}

tasks.register("runSonarAnalysis") {
    group = "Reporting"
    description = "Exécute les tests (Couverture JaCoCo) et télécharge le rapport vers SonarCloud."
    dependsOn(":library:jacocoTestReport")
    finalizedBy("sonar")
    doLast {
        println("\n🚀 CYCLE SONAR : Rapports générés. Lancement de l'upload vers SonarCloud...")
    }
}

apply(from = "sync-standards.gradle.kts")
