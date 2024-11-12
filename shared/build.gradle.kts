plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.google.protobuf") version "0.9.4"
}

group = "dev.totallyspies.spydle"
version = "1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    // Protobuf
    implementation("com.google.protobuf:protobuf-java:4.28.3")
    implementation("com.google.code.gson:gson:2.11.0")
    // Assume other modules package a slf4j backend implementation
    compileOnly("org.slf4j:slf4j-api:2.0.16")

}

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.28.3"
    }
}

sourceSets {
    main {
        java {
            srcDir("${layout.buildDirectory.get()}/generated/source/proto/main/java")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("compileJava") {
    dependsOn("generateProto")
}

tasks.bootJar {
    enabled = false
}