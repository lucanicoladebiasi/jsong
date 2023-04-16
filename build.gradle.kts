import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    antlr
    idea
    `java-library`
    kotlin("jvm") version "1.7.10"
    `maven-publish`
    signing
}

val antlrVersion = "4.9.3"
val jacksonVersion = "2.14.1"
val kotlinVersion = "1.7.10"
val junitVersion = "1.9.2"

group = "io.github.lucanicoladebiasi"
version = "1.0"

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
            from(components["kotlin"])
            pom {
                name.set(rootProject.name)
                description.set("JSONata On New Ground!")
                licenses {
                    name.set("MIT License")
                    url.set(("http://www.opensource.org/licenses/mit-license.php"))
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

tasks.generateGrammarSource {
    outputDirectory = file("${outputDirectory.path}/io/github/lucanicoladebiasi/jsong/antlr")
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

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}


