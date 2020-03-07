package de.uniks.codliners.stock_simulator.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.uniks.codliners.stock_simulator.BuildConfig
import de.uniks.codliners.stock_simulator.domain.News
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The IEX API's base url, to which endpoints are concatenated.
 * Should be the sandbox endpoint for development and the live data endpoint for production.
 */
const val IEX_API_BASE_URL = BuildConfig.IEX_API_BASE_URL

/**
 * An IEX API token.
 * Should be the sandbox token for development and the live data token for production.
 */
const val IEX_API_TOKEN = BuildConfig.IEX_API_TOKEN

const val COINGECKO_BASE_URL = BuildConfig.COINGECKO_BASE_URL

/**
 * An instance of the JSON parser [moshi](https://github.com/square/moshi).
 */
val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * The CoinGecko API interface.
 *
 * @author Jan Müller
 */
interface CoinGeckoApi {

    /**
     * Requests a [CoinGeckoMarketChart] for a CoinGecko cryptocurrency.
     *
     * @param id The CoinGecko id of the cryptocurrency.
     * @param currency The target currency of the [CoinGeckoMarketChart]. Defaults to usd.
     * @param days The maximum amount of days. Defaults to unlimited.
     * @return The requested [CoinGeckoMarketChart].
     */
    @GET("coins/{id}/market_chart")
    suspend fun historicalPrices(@Path("id") id: String, @Query("vs_currency") currency: String = "usd", @Query("days") days: String = "max"): CoinGeckoMarketChart

    /**
     * Requests a [CoinGeckoQuote] for a CoinGecko cryptocurrency.
     *
     * @param id The CoinGecko id of the cryptocurrency.
     * @return The requested [CoinGeckoQuote].
     */
    @GET("coins/{id}")
    suspend fun quote(@Path("id") id: String): CoinGeckoQuote

    /**
     * Requests all [CoinGeckoSymbol]s.
     *
     * @return A [List] containing all [CoinGeckoSymbol]s.
     */
    @GET("coins/list")
    suspend fun symbols(): List<CoinGeckoSymbol>
}

/**
 * The IEX API interface.
 *
 * @author Jonas Thelemann
 * @author Jan Müller
 */
interface IEXApi {

    /**
     * Requests a [List] of [IEXHistoricalPrice]s for an IEX share.
     *
     * @param symbol The symbol of the IEX share.
     * @param range The range of the data points. Defaults to 1m (one month).
     * @param chartCloseOnly Requests only close values if true and full data otherwise.
     * @param token The IEX API token. Defaults to [IEX_API_TOKEN].
     * @return The requested [List] of [IEXHistoricalPrice]s.
     */
    @GET("stock/{symbol}/chart/{range}")
    suspend fun historicalPrices(@Path("symbol") symbol: String, @Path("range") range: String = "1m", @Query("chartCloseOnly") chartCloseOnly: Boolean, @Query("token") token: String = IEX_API_TOKEN): List<IEXHistoricalPrice>

    /**
     * Requests [News] for an IEX share.
     *
     * @param symbol The symbol of the IEX share.
     * @param token The IEX API token. Defaults to [IEX_API_TOKEN].
     * @return The requested [List] of [News].
     */
    @GET("stock/{symbol}/news")
    suspend fun news(@Path("symbol") symbol: String, @Query("token") token: String = IEX_API_TOKEN): List<News>

    /**
     * Requests an [IEXQuote] for an IEX share.
     *
     * @param symbol The symbol of the IEX share.
     * @param token The IEX API token. Defaults to [IEX_API_TOKEN].
     * @return The requested [IEXQuote]
     */
    @GET("stock/{symbol}/quote")
    suspend fun quote(@Path("symbol") symbol: String, @Query("token") token: String = IEX_API_TOKEN): IEXQuote

    /**
     * Requests all [IEXSymbol]s.
     *
     * @param token The IEX API token. Defaults to [IEX_API_TOKEN].
     * @return A [List] containing all [IEXSymbol]s.
     */
    @GET("ref-data/symbols")
    suspend fun symbols(@Query("token") token: String = IEX_API_TOKEN): List<IEXSymbol>
}

/**
 * Provides instances for various APIs.
 *
 * @property COINGECKO_API The [CoinGeckoApi] instance.
 * @property IEX_API The [IEX_API] instance.
 * 
 * @author Jonas Thelemann
 * @author Jan Müller
 */
object NetworkService {

    private val client = OkHttpClient()

    private val converterFactory = MoshiConverterFactory.create(moshi)

    val COINGECKO_API: CoinGeckoApi = Retrofit.Builder()
        .baseUrl(COINGECKO_BASE_URL)
        .client(client)
        .addConverterFactory(converterFactory)
        .build()
        .create(CoinGeckoApi::class.java)

    val IEX_API: IEXApi = Retrofit.Builder()
        .baseUrl(IEX_API_BASE_URL)
        .client(client)
        .addConverterFactory(converterFactory)
        .build()
        .create(IEXApi::class.java)
}
