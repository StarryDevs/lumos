plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "lumos"

include(":lumos-core")

include(
    "examples:json-like",
    "examples:expression",
    "examples:tokenizers"
)