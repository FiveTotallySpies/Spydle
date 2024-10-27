plugins {
	java
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
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
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("io.netty:netty-all")
	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	// Jank Agones SDK
	implementation("net.infumia:agones4j:2.0.2")
	implementation("io.grpc:grpc-stub:1.64.0")
	implementation("io.grpc:grpc-protobuf:1.64.0")
	implementation("io.grpc:grpc-okhttp:1.68.0")
	implementation("org.apache.tomcat:annotations-api:6.0.53")
	implementation("com.google.protobuf:protobuf-java:4.28.3")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
