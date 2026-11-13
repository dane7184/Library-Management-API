plugins {
    java
    id("org.springframework.boot") version "3.5.16" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

val mapstructVersion = "1.6.3"
val lombokMapstructBindingVersion = "0.2.0"
val poiVersion = "5.3.0"
val springdocVersion = "2.8.17"

allprojects {
    group = "com.library"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")

    // Boot 3.5 manages Flyway 11.7.x, which predates PostgreSQL 18 support
    extra["flyway.version"] = "11.20.3"

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
        dependencies {
            dependency("org.mapstruct:mapstruct:$mapstructVersion")
            dependency("org.mapstruct:mapstruct-processor:$mapstructVersion")
            dependency("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")
            dependency("org.apache.poi:poi-ooxml:$poiVersion")
            dependency("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
        }
    }

    // Shared by every module
    dependencies {
        "compileOnly"("org.projectlombok:lombok")
        "annotationProcessor"("org.projectlombok:lombok")
        "annotationProcessor"("org.mapstruct:mapstruct-processor")
        "annotationProcessor"("org.projectlombok:lombok-mapstruct-binding")

        "testCompileOnly"("org.projectlombok:lombok")
        "testAnnotationProcessor"("org.projectlombok:lombok")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<JavaCompile> {
        options.release.set(21)
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
