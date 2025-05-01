dependencies {
    implementation(project(":core"))

    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")

    // websocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
}