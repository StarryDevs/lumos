plugins {
    kotlin("jvm")
    application
}

dependencies {
    testImplementation(kotlin("test"))

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
