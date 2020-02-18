package de.uniks.codliners.stock_simulator.network

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test


class NetworkServiceTest {
    @Test
    fun symbols_isCorrect() {
        runBlocking {
            Assert.assertEquals(
                symbol?.symbol,
                NetworkService.IEX_API.symbols(IEX_API_TOKEN)[0].symbol
            )
        }
    }
}
