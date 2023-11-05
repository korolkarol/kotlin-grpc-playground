package com.karolkorol.service

import com.karolkorol.grpc.countries.CountriesServiceGrpcKt
import com.karolkorol.grpc.countries.GetCityRequest
import com.karolkorol.grpc.countries.GetCityResponse
import com.karolkorol.grpc.countries.GetCountryRequest
import com.karolkorol.grpc.countries.GetCountryResponse
import com.karolkorol.grpc.countries.getCityResponse
import com.karolkorol.grpc.countries.getCountryResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.data.annotation.Id
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

@Service
class CountryService(
    private val countryRepository: CountryRepository,
    private val cityRepository: CityRepository,
) : CountriesServiceGrpcKt.CountriesServiceCoroutineImplBase() {

    private val logger = LoggerFactory.getLogger(javaClass)
    override fun getCountry(request: GetCountryRequest): Flow<GetCountryResponse> = when (request.countryFilterCase) {
        GetCountryRequest.CountryFilterCase.BYNAME -> getCountryFlow { findByName(request.byName) }
        GetCountryRequest.CountryFilterCase.BYCODE -> getCountryFlow { findByCode(request.byCode) }
        else -> flow { getCountryResponse { } }
    }

    fun getCountryFlow(finder: suspend CountryRepository.() -> Country?) = flow {
        emit(finder(countryRepository)?.asGetCountryResponse() ?: getCountryResponse { })
    }

    private suspend fun Country.asGetCountryResponse() = let { country ->
        getCountryResponse {
            name = country.name
            cities = cityRepository.findAllByCountryCode(country.code)
                .map { it.name }
                .toList(mutableListOf())
                .joinToString(",")
            population = country.population.toString()
            code = country.code
        }
    }

    val allCities = mutableMapOf<Int, GetCityResponse>()
    override fun getCity(request: GetCityRequest): Flow<GetCityResponse> = when (request.cityFilterCase) {
        GetCityRequest.CityFilterCase.BYNAME -> getCityFlow { findByName(request.byName) }
        GetCityRequest.CityFilterCase.BYCODE -> getCityFlow { findByCountryCode(request.byCode) }
        GetCityRequest.CityFilterCase.ALL -> channelFlow {
            (0..4079).map {
                launch {
                    send(cityRepository.findById(it).asGetCityResponse())
                }
            }
        }

        else -> flow { getCityResponse { } }
    }

    suspend fun <T> SendChannel<T>.sendAndReturn(t: T): T {
        send(t)
        return t
    }

    fun getCityFlow(finder: suspend CityRepository.() -> City?) = flow {
        emit(finder(cityRepository)?.asGetCityResponse() ?: getCityResponse { })
    }

    private suspend fun City?.asGetCityResponse() = this?.let { city ->
        getCityResponse {
            name = city.name
            countryCode = city.countryCode
            population = city.population.toString()
        }
    } ?: getCityResponse { }
}

@Repository
interface CityRepository : CoroutineCrudRepository<City, Int> {
    suspend fun findByName(name: String): City?
    suspend fun findByCountryCode(code: String): City?
    suspend fun findAllByCountryCode(countryCode: String): Flow<City>
}

@Repository
interface CountryRepository : CoroutineCrudRepository<Country, String> {
    suspend fun findByName(name: String): Country?
    suspend fun findByCode(code: String): Country?
}

data class City(
    @Id val id: Int?,
    val name: String,
    val countryCode: String,
    val district: String?,
    val population: Long,
    val localName: String?,
)

data class Country(
    @Id val code: String,
    val name: String,
    val continent: String,
    val region: String,
    val surfaceArea: Double,
    val indepYear: Int?,
    val population: Long,
    val lifeExpectancy: Double?,
    val gnp: Double?,
    val gnpOld: Double?,
    val localName: String,
    val governmentForm: String,
    val headOfState: String?,
    val capital: Int?,
    val code2: String,
)