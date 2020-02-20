package de.uniks.codliners.stock_simulator.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.uniks.codliners.stock_simulator.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

const val IEX_API_BASE_URL = BuildConfig.IEX_API_BASE_URL
const val IEX_API_TOKEN = BuildConfig.IEX_API_TOKEN

const val COINGECKO_BASE_URL = BuildConfig.COINGECKO_BASE_URL

val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

interface IexApi {

    @GET("ref-data/symbols")
    suspend fun symbols(@Query("token") token: String = IEX_API_TOKEN): List<IEXSymbol>

    @GET("stock/{symbol}/chart/{range}")
    suspend fun historical(@Path("symbol") symbol: String, @Path("range") range: String = "1m", @Query("token") token: String = IEX_API_TOKEN, @Query("chartCloseOnly") chartCloseOnly: Boolean): List<IEXHistoricalPrice>

    @GET("stock/{symbol}/quote")
    suspend fun quote(@Path("symbol") symbol: String, @Query("token") token: String = IEX_API_TOKEN): IEXQuote
}

interface CoinGeckoApi {

    @GET("coins/list")
    suspend fun symbols(): List<CoinGeckoSymbol>

    @GET("coins/{id}")
    suspend fun quote(@Path("id") id: String): CoinGeckoQuote

    @GET("/coins/{id}/market_chart")
    suspend fun historical(@Path("id") id: String): CoinGeckoMarketChart
}

object NetworkService {

    val IEX_API: IexApi = Retrofit.Builder()
        .baseUrl(IEX_API_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(IexApi::class.java)

    val COINGECKO_API: CoinGeckoApi = Retrofit.Builder()
        .baseUrl(COINGECKO_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(CoinGeckoApi::class.java)
}
