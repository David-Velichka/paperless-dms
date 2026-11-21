plugins {
	java
	jacoco
	id("org.springframework.boot") version "4.2.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.paperless"
version = "0.3.1"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("software.amazon.awssdk:bom:2.59.7")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-rabbitmq")
	implementation("org.springframework.boot:spring-boot-starter-jackson")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("software.amazon.awssdk:s3")
	implementation("net.sourceforge.tess4j:tess4j:5.21.0")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Coverage report (build/reports/jacoco)
tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}
