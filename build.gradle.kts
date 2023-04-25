/**
 * MIT License
 *
 * Copyright (c) 2023 Luca Nicola Debiasi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    antlr
    idea
    `java-library`
    kotlin("jvm") version "1.8.20"
    `maven-publish`
    signing
}

val antlrVersion = "4.9.3"
val jacksonVersion = "2.14.2"
val kotlinVersion = "1.7.10"
val junitVersion = "1.9.2"

group = "io.github.lucanicoladebiasi"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:$antlrVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib-jdk8"))
}

java {
    withJavadocJar()
    withSourcesJar()
}


publishing {
    publications {
        create<MavenPublication>(project.name) {
            artifactId = rootProject.name
            from(components["java"])
            pom {
                name.set(rootProject.name)
                description.set("JSONata On New Ground!")
                url.set("https://github.com/lucanicoladebiasi/jsong")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/license/mit/")
                    }
                }
                developers {
                    developer {
                        id.set("lucanicoladebiasi")
                        name.set("Luca Nicola Debiasi")
                        email.set("luca.nicola.debiasi@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/lucanicoladebiasi/jsong")
                    developerConnection.set("scm:git:ssh://github.com/lucanicoladebiasi/jsong")
                    url.set("https://github.com/lucanicoladebiasi/jsong")
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/repos/releases")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/repos/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = providers.gradleProperty("repoUser").get()
                password = providers.gradleProperty("repoPassword").get()
            }
        }
    }
}

signing {
    sign(publishing.publications[project.name])
}

tasks.compileTestKotlin {
    dependsOn(tasks.generateTestGrammarSource)
}



tasks.generateGrammarSource {
    outputDirectory = file("${outputDirectory.path}/io/github/lucanicoladebiasi/jsong/antlr")
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    dependsOn("generateGrammarSource")
    kotlinOptions.jvmTarget = "1.8"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}


