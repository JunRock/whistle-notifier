import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") version "2.2.20" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = "io.junseok"
    version = "0.1.0"

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

    configure<JavaPluginExtension> { withSourcesJar() }
    configure<PublishingExtension> {
        publications { create<MavenPublication>("maven") { from(components["java"]) } }
    }

    dependencies { "testImplementation"("org.jetbrains.kotlin:kotlin-test") }
    tasks.withType<Test> { useJUnitPlatform() }
}
