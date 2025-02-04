plugins {
    id("java")
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.microsoft.azure.azurefunctions") version "1.8.2"
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
    implementation("com.microsoft.azure.functions:azure-functions-java-library:1.4.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.test {
    useJUnitPlatform()
}

gradle.taskGraph.whenReady {
    project.extensions.extraProperties["azurefunctions.appName"] = "PhotoCatalogFunctionApp"
    project.extensions.extraProperties["azurefunctions.resourceGroup"] = "PhotoCatalogSecondary"
    project.extensions.extraProperties["azurefunctions.region"] = "West US"
    project.extensions.extraProperties["azurefunctions.pricingTier"] = "Consumption"
    project.extensions.extraProperties["azurefunctions.runtime.os"] = "linux"
    project.extensions.extraProperties["azurefunctions.runtime.javaVersion"] = "21"
}
