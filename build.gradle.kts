import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    antlr
    idea
    kotlin("jvm") version "1.7.10"
}

val antlr_version = "4.9.3"
val jackson_version = "2.13.3"
val kotlin_version = "1.7.10"

group = "org.jsong"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:$antlr_version")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jackson_version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    testImplementation(kotlin("test"))
}

tasks.generateGrammarSource {
    outputDirectory = file("${outputDirectory.path}/org/jsong/antlr")
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages")
}

tasks.compileTestKotlin {
    dependsOn(tasks.generateTestGrammarSource)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    dependsOn("generateGrammarSource")
    kotlinOptions.jvmTarget = "1.8"
}
