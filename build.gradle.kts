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
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("org.testcontainers:junit-jupiter:1.20.5")
    testRuntimeOnly("org.testcontainers:gcloud:1.20.5")
}

tasks.test {
    useJUnitPlatform()
}
