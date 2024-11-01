plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
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
	// Shared
	implementation(project(":shared"))
	// SpringBoot
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	// Redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("redis.clients:jedis:5.1.2")
	implementation("org.springframework.data:spring-data-redis")
	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	// K8s
	implementation("io.kubernetes:client-java:21.0.2")
	// Agones GRPC wrapper
	implementation("net.infumia:agones4j:2.0.2")
	// GRPC and Protobuf for Agones
	implementation("io.grpc:grpc-stub:1.64.0")
	implementation("io.grpc:grpc-protobuf:1.64.0")
	implementation("io.grpc:grpc-okhttp:1.68.0")
	implementation("org.apache.tomcat:annotations-api:6.0.53")
	implementation("com.google.protobuf:protobuf-java:4.28.3")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.openapitools:openapi-generator-gradle-plugin:7.9.0")
	}
}


openApiGenerate {
	skipValidateSpec = true
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