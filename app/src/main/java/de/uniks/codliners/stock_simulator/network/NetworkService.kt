package de.uniks.codliners.stock_simulator.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.uniks.codliners.stock_simulator.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val IEX_API_BASE_URL = BuildConfig.IEX_API_BASE_URL

val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

interface IexApi {

    @GET("ref-data/symbols")
    suspend fun symbols(@Query("token") token: String): SymbolsResponse
}

object NetworkService {
    private val retrofit = Retrofit.Builder()
        .baseUrl(IEX_API_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val IEX_API: IexApi = retrofit.create(IexApi::class.java)
}
