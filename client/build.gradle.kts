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

tasks.register("runClient", JavaExec::class) {
    dependsOn += "build"
    group = "Run"
    doFirst {
        println("classpath=${sourceSets.main.get().runtimeClasspath.asPath}")
    }
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.karolkorol.ProductClientKt"
    doLast{
        println("client finished")
    }
}