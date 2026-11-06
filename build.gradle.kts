plugins {
	alias(libs.plugins.java)
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.spring.dependency.management)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
	}
}

repositories {
	mavenCentral()
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

dependencies {
	implementation(libs.spring.boot.starter.webmvc)
	implementation(libs.spring.boot.starter.webmvc.ui)

	implementation(libs.spring.boot.starter.data.jpa)
	// Core Spring Security support
	implementation(libs.spring.boot.starter.security)
	// Essential utilities for testing secured endpoints
	implementation(libs.spring.boot.starter.validation)
	implementation(libs.mapstruct.api)
	implementation(libs.spring.boot.starter.oauth2 )
	implementation(libs.spring.boot.starter.mail)
	compileOnly(libs.lombok)
	annotationProcessor(libs.lombok)
	annotationProcessor(libs.mapstruct.processor)
	testImplementation(libs.spring.security.test)
	runtimeOnly(libs.postgresql)
	testImplementation(libs.spring.boot.starter.webmvc.test)
	testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
	useJUnitPlatform()
}
