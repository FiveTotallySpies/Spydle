plugins {
  java
  id("org.springframework.boot") version "3.3.5"
  id("io.spring.dependency-management") version "1.1.6"
  id("com.google.protobuf") version "0.9.4"
}

/* Protobuf config */

sourceSets {
    main {
        proto {
            srcDir("../proto")
        }
        java {
            // include self written and generated code
            srcDirs("src/main/java", "${layout.buildDirectory.get()}/generated/source/proto/main/java")
        }
    }
}

tasks.named("compileJava") {
    dependsOn("generateProto")
}

/* Protobuf config end */

group = "dev.totallyspies"
version = "0.0.1-SNAPSHOT"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.google.protobuf:protobuf-java:4.28.3")
  implementation("org.springframework.boot:spring-boot-starter-web")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
  useJUnitPlatform()
}