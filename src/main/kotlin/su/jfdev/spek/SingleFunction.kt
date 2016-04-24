package su.jfdev.spek

/**
 * Jamefrus and his team on 24.04.2016.
 */

class SingleFunction constructor(private var function: () -> Boolean): () -> Unit {

    fun use(): Boolean {
        val result = function()
        if (result) function = usedFunction
        return result
    }

    override fun invoke(): Unit {
        use()
    }

    companion object {
        val usedFunction: () -> Boolean = { false }
        inline fun make(crossinline function: () -> Unit) = SingleFunction {
            function()
            true
        }
    }
}


inline fun useSingle(crossinline action: () -> Unit, block: (() -> Unit) -> Unit) {
    val function = SingleFunction.make(action)
    block(function)
}


