package de.uniks.codliners.stock_simulator

import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper

const val SHARED_PREFERENCES_KEY = "de.uniks.codliners.stock_simulator"

fun ContextWrapper.sharedPreferences() = getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE)

fun Any.truly() = true
