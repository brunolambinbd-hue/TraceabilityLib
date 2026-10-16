plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.sonarqube)
}

sonarqube {
    properties {
        property("sonar.projectKey", "TraceabilityLib")
        property("sonar.projectName", "TraceabilityLib")
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.rootDir}/library/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        property("sonar.sources", "library/src/main/java")
        property("sonar.tests", "library/src/test/java")
        property("sonar.java.binaries", "library/build/tmp/kotlin-classes/debug")
    }
}

apply(from = "sync-standards.gradle.kts")
