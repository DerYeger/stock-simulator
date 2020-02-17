package de.uniks.codliners.stock_simulator.ui.share

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.uniks.codliners.stock_simulator.repository.SearchRepository

class ShareViewModel(application: Application, val shareId: String) : ViewModel() {

    private val shareRepository = SearchRepository(application)

    val share = shareRepository.shareWithId(shareId)

    class Factory(
        private val application: Application,
        private val shareId: String
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShareViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShareViewModel(application, shareId) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
