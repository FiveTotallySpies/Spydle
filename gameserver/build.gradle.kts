plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
	id("com.google.protobuf") version "0.9.4"
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
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-tomcat")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	// Redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("redis.clients:jedis:5.1.2")
	implementation("org.springframework.data:spring-data-redis")
	// Kubernetes
	implementation("io.kubernetes:client-java:21.0.2")
	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	// Agones GRPC wrapper
	implementation("net.infumia:agones4j:2.0.2")
	// GRPC and Protobuf for Agones + Sockets
	implementation("io.grpc:grpc-stub:1.64.0")
	implementation("io.grpc:grpc-protobuf:1.64.0")
	implementation("io.grpc:grpc-okhttp:1.68.0")
	implementation("org.apache.tomcat:annotations-api:6.0.53")
	implementation("com.google.protobuf:protobuf-java:4.28.3")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:4.28.3"
	}
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
	modelPackage.set("dev.totallyspies.spydle.gameserver.generated.model")
	apiPackage.set("dev.totallyspies.spydle.gameserver.generated.api")
	invokerPackage.set("dev.totallyspies.spydle.gameserver.generated.invoker")
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
	dependsOn("openApiGenerate")
	dependsOn("generateProto")
}

tasks.bootJar {
	archiveFileName.set("gameserver.jar")
}

tasks.jar {
	enabled = false
}