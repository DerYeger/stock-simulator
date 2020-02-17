package de.uniks.codliners.stock_simulator.ui.account

import androidx.lifecycle.ViewModel
import de.uniks.codliners.stock_simulator.domain.Share

class AccountViewModel : ViewModel() {

    val depotShares = listOf(
        Share("1", "SMA Solar", 45.5, -5.3, 0.23),
        Share("2", "Daimler", 21.0, 28.9, 2.45)
    )

}