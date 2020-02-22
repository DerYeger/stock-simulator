@file:Suppress("SpellCheckingInspection")

package de.uniks.codliners.stock_simulator.network

import com.squareup.moshi.JsonAdapter
import de.uniks.codliners.stock_simulator.domain.Symbol

var jsonAdapter: JsonAdapter<Symbol> = moshi.adapter<Symbol>(Symbol::class.java)
var symbol: Symbol? = jsonAdapter.fromJson("{\"symbol\":\"A\",\"exchange\":\"NYS\",\"name\":\"nchllgnie.teA ng IooisceT\",\"date\":\"2020-02-17\",\"isEnabled\":true,\"type\":\"cs\",\"region\":\"US\",\"currency\":\"USD\",\"iexId\":\"IEX_46574843354B2D52\"}")