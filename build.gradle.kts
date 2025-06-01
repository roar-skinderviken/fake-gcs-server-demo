plugins {
    java
}

version = "0.1"
group = "no.javatec"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("com.google.cloud:google-cloud-storage:2.52.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.1")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.testcontainers:gcloud:1.21.1")
}

tasks.test {
    useJUnitPlatform()
}
