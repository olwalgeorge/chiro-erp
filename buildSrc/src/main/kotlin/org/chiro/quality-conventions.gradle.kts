// Quality conventions for Chiro ERP consolidated services
// Code quality, formatting, and static analysis

plugins {
    id("com.diffplug.spotless")
    id("io.gitlab.arturbosch.detekt")
}

// Spotless configuration for consistent code formatting
spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint("1.0.1")
        indentWithSpaces(4)
        endWithNewline()
        trimTrailingWhitespace()
    }
    
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.0.1")
        indentWithSpaces(4)
        endWithNewline()
        trimTrailingWhitespace()
    }
}

// Detekt configuration for static analysis
detekt {
    buildUponDefaultConfig = true
    allRules = false
    
    config.setFrom("config/detekt.yml")
    baseline = file("config/baseline.xml")
    
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
    }
}

// Quality gates
tasks.register("qualityCheck") {
    description = "Runs all quality checks"
    group = "verification"
    
    dependsOn("spotlessCheck", "detekt")
}

// Auto-fix formatting issues
tasks.register("qualityFix") {
    description = "Fixes code quality issues where possible"
    group = "formatting"
    
    dependsOn("spotlessApply")
}

// Ensure quality checks run before build
tasks.build {
    dependsOn("qualityCheck")
}

// Make quality checks part of the check task
tasks.check {
    dependsOn("qualityCheck")
}
