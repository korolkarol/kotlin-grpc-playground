plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":stub"))
    runtimeOnly("io.grpc:grpc-netty:1.59.0")
}