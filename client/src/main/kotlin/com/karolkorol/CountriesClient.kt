package com.karolkorol

import com.karolkorol.grpc.countries.All
import com.karolkorol.grpc.countries.CountriesServiceGrpcKt
import com.karolkorol.grpc.countries.GetCityRequest
import com.karolkorol.grpc.countries.GetCountryRequest
import com.karolkorol.grpc.countries.getCityRequest
import com.karolkorol.grpc.countries.getCountryRequest
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class CountriesClient(private val channel: ManagedChannel) : Closeable {

    private val stub = CountriesServiceGrpcKt.CountriesServiceCoroutineStub(channel)

    suspend fun getCity(getCityRequest: GetCityRequest) {
        val msgs = mutableListOf<String>()
        stub.getCity(getCityRequest).collect { response ->
            println(response)
            msgs.add(response.toString())
        }
        println("size: ${msgs.size}")
    }

    suspend fun getCountry(getCountryRequest: GetCountryRequest) {
        val msgs = mutableListOf<String>()
        stub.getCountry(getCountryRequest).collect { response ->
            println(response)
        }
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}

suspend fun main() {
    val port = 50052

    val channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build()

    val client = CountriesClient(channel)

    val countryByNameTime = measureTimeMillis {
        println("GetCountryByName:")
        client.getCountry(getCountryRequest { byName = "Poland" })
    }
    val countryByCodeTime = measureTimeMillis {
        println("GetCountryByCode:")
        client.getCountry(getCountryRequest { byCode = "POL" })
    }
    val cityByNameTime = measureTimeMillis {
        println("GetCityByCode:")
        client.getCity(getCityRequest { byName = "Gdansk" })
    }
    val allCitiesTime = measureTimeMillis {
        println("GetAllCities:")
        client.getCity(getCityRequest { all = All.ALL })
    }

    println(
        """
        countryByNameTime: ${countryByNameTime}ms
        countryByCodeTime: ${countryByCodeTime}ms
        cityByNameTime: ${cityByNameTime}ms
        allCitiesTime: ${allCitiesTime}ms
    """.trimIndent()
    )
}