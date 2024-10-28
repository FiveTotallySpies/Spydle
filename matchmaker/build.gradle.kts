plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
	id("org.openapi.generator") version "7.8.0"
}

group = "dev.totallyspies.spydle"
version = "0.0.1-SNAPSHOT"

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
	// SpringBoot
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	// K8s
	implementation("io.kubernetes:client-java:21.0.2")
	implementation("io.grpc:grpc-stub:1.64.0")
	implementation("io.grpc:grpc-protobuf:1.64.0")
	implementation("org.apache.tomcat:annotations-api:6.0.53")
	// OpenAPI
}

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.openapitools:openapi-generator-gradle-plugin:7.8.0")
	}
}


openApiGenerate {
	generatorName.set("java")
	inputSpec.set("$rootDir/src/main/resources/openapi.yaml")
	outputDir.set("${layout.buildDirectory.get()}/generated")
	modelPackage.set("dev.totallyspies.spydle.matchmaker.generated.model")
	apiPackage.set("dev.totallyspies.spydle.matchmaker.generated.api")
	invokerPackage.set("dev.totallyspies.spydle.matchmaker.generated.invoker")
}

sourceSets {
	main {
		java {
			srcDir("${layout.buildDirectory.get()}/generated/src/main/java")
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("compileJava") {
	dependsOn("openApiGenerate")
}

tasks.bootJar {
	archiveFileName.set("matchmaker.jar")
}

tasks.jar {
	enabled = false
}