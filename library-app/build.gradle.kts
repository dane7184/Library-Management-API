plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":library-employee"))
    implementation(project(":library-customer"))
    implementation(project(":library-book"))
    implementation(project(":library-borrow"))
    implementation(project(":library-return"))
    implementation(project(":library-scheduler"))
    implementation(project(":library-report"))

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.security:spring-security-test")
}

tasks.bootJar {
    archiveFileName.set("library-app.jar")
}

tasks.jar {
    enabled = false
}
