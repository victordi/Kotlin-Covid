plugins {
    kotlin("jvm") version "1.5.31"
}

repositories {
    mavenCentral()
}

val vertxVersion   = "4.2.1"
val log4jVersion   = "2.11.1"

dependencies {
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.2.0")
    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.0.0")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")


    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.4.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}
