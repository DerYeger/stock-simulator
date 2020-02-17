package de.uniks.codliners.stock_simulator.ui.account

import androidx.lifecycle.ViewModel
import de.uniks.codliners.stock_simulator.domain.Share

class AccountViewModel : ViewModel() {

    val depotShares = listOf(Share(1), Share(42))
}