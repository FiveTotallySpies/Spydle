plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
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

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.bootJar {
	archiveFileName.set("gameserver.jar")
}

tasks.jar {
	enabled = false
}