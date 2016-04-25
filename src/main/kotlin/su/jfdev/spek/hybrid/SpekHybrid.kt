package su.jfdev.spek.hybrid

import org.jetbrains.spek.api.DescribeBody
import org.jetbrains.spek.api.DescribeParser
import org.jetbrains.spek.junit.JUnitDescriptionCache
import org.jetbrains.spek.junit.JUnitNotifier
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter.Kind.INSTANCE
import kotlin.reflect.KotlinReflectionInternalError
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

/**
 * Jamefrus and his team on 24.04.2016.
 */
@RunWith(SpekHybrid::class)
open class SpekLink(val className: String? = null)

class SpekHybrid(var specificationClass: Class<*>): Runner() {

    init {
        if (SpekLink::class.java.isAssignableFrom(specificationClass)) {
            val spekLink = specificationClass.asSubclass(SpekLink::class.java).newInstance()
            val pack = specificationClass.`package`?.name?.plus(".") ?: ""
            val name = "$pack${spekLink.className ?: spekLink.javaClass.simpleName}"
            specificationClass = try {
                Class.forName(name)
            } catch(first: ClassNotFoundException) {
                try {
                    Class.forName(name + "Kt")
                } catch(second: ClassNotFoundException) {
                    first.printStackTrace()
                    second.printStackTrace()
                    specificationClass
                }
            }
        }
    }

    private val tree = let {
        val objectInstance = try {
            specificationClass.kotlin.objectInstance
        } catch(e: KotlinReflectionInternalError) {
            null
        }
        val description = specificationClass.getAnnotation(Spec::class.java)?.nullableName  ?: specificationClass.simpleName
        DescribeParser().run {
            describe(description) {
                functions().forEach {
                    buildBody(it, objectInstance)
                }
            }
            children().first()
        }
    }

    fun functions(): List<KFunction<*>> {
        return specificationClass.methods.filter {
            it.annotations.any { it is Test || it is Spec }
        }.map {
            it.kotlinFunction
        }.filterNotNull()
    }

    private fun DescribeBody.buildBody(function: KFunction<*>, objectInstance: Any?) {
        fun invokeFunc(body: DescribeBody) {
            when (function.parameters.size) {
                0 -> function.call()
                1 -> function.call(if (function.parameters.first().kind == INSTANCE) objectInstance else body)
                2 -> function.call(objectInstance, body)
            }
        }

        val description = function.annotations.filterIsInstance<Spec>().firstOrNull()?.nullableName ?: function.name
        if (function.isDescribe()) describe(description) {
            invokeFunc(this)
        } else it(description) {
            invokeFunc(this)
        }
    }


    private fun KFunction<*>.isDescribe() = parameters.any { it.type.javaType == DescribeBody::class.java }

    private val junitDescriptionCache = JUnitDescriptionCache()

    override fun getDescription() = junitDescriptionCache.get(tree)

    override fun run(junitNotifier: RunNotifier) = tree.run(JUnitNotifier(junitNotifier, junitDescriptionCache))

}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class Spec(val describeName: String = "")

private val Spec.nullableName: String?
    get() = if (describeName.isBlank()) null
    else describeName
