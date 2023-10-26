plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}


kotlin {
    sourceSets.getByName("main").resources.srcDir("src/main/proto")
}