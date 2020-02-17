package de.uniks.codliners.stock_simulator.ui

class OnClickListener<T>(private val block: (T) -> Unit) {
    fun onClick(input: T) = block(input)
}
