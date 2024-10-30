plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
	id("com.google.protobuf") version "0.9.4"
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
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-tomcat")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
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
	archiveFileName.set("gameserver.jar")
}

tasks.jar {
	enabled = false
}