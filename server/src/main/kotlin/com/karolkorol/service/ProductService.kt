package com.karolkorol.service

import com.karolkorol.grpc.product.GetProductRequest
import com.karolkorol.grpc.product.GetProductResponse
import com.karolkorol.grpc.product.ProductInfo
import com.karolkorol.grpc.product.ProductServiceGrpcKt
import com.karolkorol.grpc.product.getProductResponse
import com.karolkorol.grpc.product.productInfo
import com.karolkorol.grpc.product.productPrice
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.random.Random

class ProductService : ProductServiceGrpcKt.ProductServiceCoroutineImplBase() {
    override fun get(request: GetProductRequest): Flow<GetProductResponse> = channelFlow {
        val productInfo = sendAndReturnProductInfoAsync(request)
        sendProductPrice(productInfo)
    }

    private fun ProducerScope<GetProductResponse>.sendProductPrice(
        productInfo: Deferred<ProductInfo>,
    ) {
        launch {
            delay(Random.nextLong(500, 1500))
            send(
                getProductResponse {
                    productPrice = prices[productInfo.await().ean] ?: productPrice {}
                }
            )
        }
    }

    private fun ProducerScope<GetProductResponse>.sendAndReturnProductInfoAsync(request: GetProductRequest) =
        async {
            delay(Random.nextLong(10, 150))
            val info = products.firstOrNull { it.ean == request.eam } ?: productInfo {}
            send(
                getProductResponse {
                    productInfo = info
                }
            )
            return@async info
        }

    companion object {
        const val PLN = "PLN"
        val priceFormat = DecimalFormat("0.00 $PLN")

        val products = listOf(
            productInfo {
                ean = "1234567890123"
                mdk = "A111A-10A"
                sizes += listOf("S", "M", "L", "XL")
            },

            productInfo {
                ean = "2222222222222"
                mdk = "B222B-20B"
                sizes += listOf("M", "L", "XL")
            },

            productInfo {
                ean = "3333333333333"
                mdk = "B333B-30B"
                sizes += listOf("M", "XL")
            },
        )

        fun price(
            currentPriceValue: Double,
            previousPriceValue: Double,
        ) = productPrice {
            currentPrice = currentPriceValue
            previousPrice = previousPriceValue
            currency = PLN
            formattedCurrentPrice = priceFormat.format(currentPriceValue)
            formattedPreviousPrice = priceFormat.format(previousPriceValue)
        }

        val prices = mapOf(
            products[0].ean to price(9.99, 14.99),
            products[1].ean to price(99.99, 149.99),
            products[2].ean to price(49.99, 84.99),
        )
    }
}