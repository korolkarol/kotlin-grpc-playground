package com.karolkorol

import com.karolkorol.service.CountryService
import com.karolkorol.service.ProductService
import io.grpc.Server
import io.grpc.ServerBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component


@SpringBootApplication
open class GrpcServerApplicationRunner(
    private val grpcServer: GrpcServer,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        grpcServer.start()
        grpcServer.blockUntilShutdown()
    }
}

@Component
class GrpcServer(
    @Value("\${grpc.server.port}") private val port: Int,
    productService: ProductService,
    countryService: CountryService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    val server: Server = ServerBuilder
        .forPort(port)
        .addService(productService)
        .addService(countryService)
        .build()

    fun start() {
        server.start()
        logger.info("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info("shutting down gRPC server since JVM is shutting down")
                this@GrpcServer.stop()
                logger.info("server shut down")
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

fun main(vararg args: String) {
    runApplication<GrpcServerApplicationRunner>(*args)
}