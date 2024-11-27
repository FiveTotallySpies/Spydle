plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    application
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

application {
    mainClass.set("dev.totallyspies.spydle.frontend.FrontendApplication")
}

repositories {
    mavenCentral()
}

dependencies {
    // Shared
    implementation(project(":shared"))
    // SpringBoot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    // Protobuf
    implementation("com.google.protobuf:protobuf-java:4.28.3")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}