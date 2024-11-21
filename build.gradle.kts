plugins {
    id("java")
    id ("org.springframework.boot") version ("3.3.4")
    id ("io.spring.dependency-management") version ("1.1.6")
}

group = "org.app"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.azure:azure-ai-vision-imageanalysis:0.15.1-beta.1")
    implementation("com.azure:azure-core-http-netty:1.13.6")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("org.springframework.boot:spring-boot-starter-webflux:2.5.3")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
    implementation("com.azure:azure-storage-blob:12.23.0")




    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}


tasks.test {
    useJUnitPlatform()
}