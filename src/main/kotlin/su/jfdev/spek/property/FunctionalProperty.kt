@file:Suppress("unused")

package su.jfdev.spek.property

/**
 * Jamefrus and his team on 24.04.2016.
 */

class FunctionalProperty<T>(val get: () -> T, val set: (T) -> Unit) {
    var value: T
        get() = get()
        set(value) = set(value)

    fun  <S> switcher(switcher: (S) -> T) = PropertySwitcher(this, switcher)
    fun trigger(switcher: (Boolean) -> T) = PropertySwitcher(this, switcher)
}

fun <T> property(getter: () -> T, setter: (T) -> Unit) = FunctionalProperty(getter, setter)

open class PropertySwitcher<T, S>(val functionalProperty: FunctionalProperty<T>, val switcher: (S) -> T) {
    private var lastCase: S? = null
    fun switch(case: S) {
        functionalProperty.value = switcher(case)
    }

    fun caseIs(case: S) = case == lastCase
}

fun <T> PropertySwitcher<T, Boolean>.turnOn() = this.switch(true)
fun <T> PropertySwitcher<T, Boolean>.turnOff() = this.switch(false)
fun <T> PropertySwitcher<T, Boolean>.turn() = switch(if (caseIs(true)) false else true)

fun <T> FunctionalProperty<T>.trigger(onTrue: T, onFalse: T): PropertySwitcher<T, Boolean> = trigger<T>({ onTrue }, { onFalse })

inline fun <T> FunctionalProperty<T>.trigger(crossinline isTrue: () -> T, crossinline isFalse: () -> T) = trigger {
    if (it) isTrue()
    else isFalse()
}

inline fun <T> FunctionalProperty<T>.triggerWithModify(crossinline modify: (T) -> T): PropertySwitcher<T, Boolean> {
    val first = value
    val second = modify(first)
    return trigger {
        if (it) second else first
    }
}