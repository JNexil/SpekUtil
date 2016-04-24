package su.jfdev.spek

import org.jetbrains.spek.api.DescribeBody
import su.jfdev.spek.property.PropertySwitcher

/**
 * Jamefrus and his team on 24.04.2016.
 */

fun <T> DescribeBody.givenData(iterable: Iterable<T>, describe: DescribeBody.(T) -> Unit) = givenData("%s", iterable, describe)

fun <T> DescribeBody.givenData(format: String, iterable: Iterable<T>, describe: DescribeBody.(T) -> Unit) = givenData({ format.format(it.toString()) }, iterable, describe)

inline fun <T> DescribeBody.givenData(format: (T) -> String, iterable: Iterable<T>, crossinline describeBody: DescribeBody.(T) -> Unit) {
    iterable.forEach {
        describe(format(it)) {
            describeBody(it)
        }
    }
}

inline fun DescribeBody.singleBefore(crossinline action: () -> Unit) = useSingle(action) {
    beforeEach {
        it()
    }
}

inline fun DescribeBody.singleAfter(crossinline action: () -> Unit) = useSingle(action) {
    afterEach {
        it()
    }
}
fun <T> DescribeBody.switchAtTime(trigger: PropertySwitcher<T, Boolean>, first: Boolean = true) {
    beforeEach {
        trigger.switch(first)
    }

    beforeEach {
        trigger.switch(!first)
    }
}

fun <T> DescribeBody.singleSwitch(trigger: PropertySwitcher<T, Boolean>, first: Boolean = true) {
    singleBefore {
        trigger.switch(first)
    }

    singleAfter {
        trigger.switch(!first)
    }
}