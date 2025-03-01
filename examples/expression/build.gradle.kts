plugins {
    kotlin("jvm")
    application
}

dependencies {
    testImplementation(kotlin("test"))

    api("org.fusesource.jansi:jansi:2.4.1")
    api(project(":lumos-core"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "MainKt"
}
