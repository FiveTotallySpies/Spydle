plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.google.protobuf") version "0.9.4"
    id("org.openapi.generator") version "7.9.0"
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
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("io.gsonfire:gson-fire:1.9.0")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    // Assume other modules package a slf4j backend implementation
    compileOnly("org.slf4j:slf4j-api:2.0.16")

}

buildscript {
    repositories {
        mavenCentral()
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

openApiGenerate {
    generatorName.set("java")
    inputSpec.set("${layout.projectDirectory}/src/main/resources/openapi.yaml")
    outputDir.set("${layout.buildDirectory.get()}/generated")
    modelPackage.set("dev.totallyspies.spydle.matchmaker.generated.model")
    apiPackage.set("dev.totallyspies.spydle.matchmaker.generated.api")
    invokerPackage.set("dev.totallyspies.spydle.matchmaker.generated.invoker")

    schemaMappings.put("GameServer", "dev.totallyspies.spydle.shared.model.GameServer")
    schemaMappings.put("ClientSession", "dev.totallyspies.spydle.shared.model.ClientSession")
}

sourceSets {
    main {
        java {
            srcDir("${layout.buildDirectory.get()}/generated/source/proto/main/java")
            srcDir("${layout.buildDirectory.get()}/generated/src/main/java")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("compileJava") {
    dependsOn("generateProto")
    dependsOn("openApiGenerate")
}

tasks.bootJar {
    enabled = false
}