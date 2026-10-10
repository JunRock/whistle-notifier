import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") version "2.2.20" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
    id("com.vanniktech.maven.publish") version "0.34.0" apply false
}

val repoUrl = "https://github.com/JunRock/whistle-notifier"

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "com.vanniktech.maven.publish")
    apply(plugin = "signing")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = "io.github.junrock"
    version = "0.0.2"

    repositories { mavenCentral() }

    /*
     * Java 17 바이트코드로 빌드, 사용처가 17이어도 호환
     */
    configure<KotlinJvmProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjdk-release=17")
        }
    }
    tasks.withType<JavaCompile> { options.release.set(17) }

    configure<KtlintExtension> { version.set("1.5.0") }

    configure<SigningExtension> { useGpgCmd() }
    configure<MavenPublishBaseExtension> {
        publishToMavenCentral()
        signAllPublications()
        pom {
            name.set(project.name)
            description.set("Lightweight notification library for Kotlin/Spring Boot backends")
            url.set(repoUrl)
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("junrock")
                    name.set("SeokJun")
                    url.set("https://github.com/JunRock")
                }
            }
            scm {
                url.set(repoUrl)
                connection.set("scm:git:$repoUrl.git")
                developerConnection.set("scm:git:ssh://git@github.com/JunRock/whistle-notifier.git")
            }
        }
    }

    dependencies { "testImplementation"("org.jetbrains.kotlin:kotlin-test") }
    tasks.withType<Test> { useJUnitPlatform() }
}
