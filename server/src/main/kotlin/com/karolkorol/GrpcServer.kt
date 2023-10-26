package com.karolkorol

import com.karolkorol.service.ProductService
import io.grpc.Server
import io.grpc.ServerBuilder

class GrpcServer(private val port: Int) {
    val server: Server = ServerBuilder
        .forPort(port)
        .addService(ProductService())
        .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@GrpcServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

}

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 50052
    val server = GrpcServer(port)
    server.start()
    server.blockUntilShutdown()
}