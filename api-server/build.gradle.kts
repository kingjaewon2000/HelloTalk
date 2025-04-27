import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("plugin.jpa")
    id("org.springframework.boot")
}

val jdslVersion: String by project

tasks.withType<BootJar> {
    enabled = true
}

tasks.withType<Jar> {
    enabled = false
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.mindrot:jbcrypt:0.4")
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // kotlin-jdsl
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:$jdslVersion")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:$jdslVersion")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:$jdslVersion")

    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")
}