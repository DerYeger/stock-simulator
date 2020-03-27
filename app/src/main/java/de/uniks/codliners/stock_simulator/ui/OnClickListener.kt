package de.uniks.codliners.stock_simulator.ui

/**
 * Utility class for type safe click event handling.
 *
 * @param T The input type.
 * @property block Code block that is run once [onClick] is called.
 *
 * @author Jan Müller
 */
class OnClickListener<T>(private val block: (T) -> Unit) {

    /**
     * Calls the code block with the passed input.
     *
     * @param input The input for the code block.
     */
    fun onClick(input: T) = block(input)
}
