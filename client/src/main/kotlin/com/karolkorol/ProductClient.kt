package com.karolkorol

import com.karolkorol.grpc.product.GetProductResponse
import com.karolkorol.grpc.product.ProductServiceGrpcKt
import com.karolkorol.grpc.product.getProductRequest
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.io.Closeable
import java.util.concurrent.TimeUnit

class ProductClient(private val channel: ManagedChannel) : Closeable {

    private val stub = ProductServiceGrpcKt.ProductServiceCoroutineStub(channel)

    suspend fun getProduct(eanValue: String) {
        val msgs = mutableListOf<String>()
        stub.getProduct(
            getProductRequest {
                ean = eanValue
            }
        ).collect { response ->
            println(response)

            when (response.productDataCase) {
                GetProductResponse.ProductDataCase.PRODUCTSTOCK -> {
                    response.productStock.sizesStockList
                        .filter { it.stock == 0 }
                        .map { "size ${it.size} is not in stock!" }
                        .onEach { msgs.add(it) }
                }

                GetProductResponse.ProductDataCase.PRODUCTPRICE -> {
                    response.productPrice.currency.takeIf { it == "PLN" }
                        ?.let { msgs.add("currency is PLN") }
                }

                else -> {}
            }
        }

        msgs.joinToString(separator = "\n") { "\t* $it" }
            .takeIf { it.isNotEmpty() }
            ?.let { println("Messages:\n $it") }
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}

suspend fun main() {
    val port = 50052

    val channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build()

    val client = ProductClient(channel)

    client.getProduct("1234567890123")
}