/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("kotlin-kapt")
    id("maven-publish")
    id("java-library")
}

apply(plugin = "java")
apply(plugin = "kotlin")

group = "com.icerockdev.service"
version = "0.0.2"

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

dependencies {
    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${properties["kotlin_version"]}")
    // logging
    implementation ("ch.qos.logback:logback-classic:${properties["logback_version"]}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${properties["coroutines_version"]}")

    // https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
    api(group = "org.apache.kafka", name = "kafka-clients", version = properties["kafka_api"].toString())

    // json support
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-joda
    implementation(group = "com.fasterxml.jackson.datatype", name = "jackson-datatype-joda", version = properties["jackson_version"].toString())
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
    api(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = properties["jackson_version"].toString())
    api(group = "com.fasterxml.jackson.core", name = "jackson-core", version = properties["jackson_version"].toString())
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
    api(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = properties["jackson_version"].toString())
    implementation("joda-time:joda-time:${properties["jodatime_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

repositories {
    mavenCentral()
}

publishing {
    repositories.maven("https://api.bintray.com/maven/icerockdev/backend/kafka-service/;publish=1") {
        name = "bintray"

        credentials {
            username = System.getProperty("BINTRAY_USER")
            password = System.getProperty("BINTRAY_KEY")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
