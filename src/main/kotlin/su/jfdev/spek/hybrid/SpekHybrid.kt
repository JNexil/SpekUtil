package su.jfdev.spek.hybrid

import org.jetbrains.spek.api.DescribeBody
import org.jetbrains.spek.api.DescribeParser
import org.jetbrains.spek.api.SpekTree
import org.jetbrains.spek.junit.JUnitDescriptionCache
import org.jetbrains.spek.junit.JUnitNotifier
import org.junit.Test
import org.junit.runner.Description
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

class SpekHybrid(val specificationClass: Class<*>): Runner() {
    private val junitDescriptionCache = JUnitDescriptionCache()
    var tree: SpekTree

    init {
        val objectInstance = objectInstance()
        val spekFun = specFunctions()
        val parentDescribeBody = DescribeParser()
        parentDescribeBody.describe(specificationClass.getAnnotation(Spec::class.java)?.nullableName ?: specificationClass.simpleName){
            spekFun.forEach {
                buildBody(it, objectInstance)
            }
        }
        tree = parentDescribeBody.children()[0]

    }

    private fun objectInstance(): Any? {
        return try {
            specificationClass.kotlin.objectInstance
        } catch(e: KotlinReflectionInternalError) {
            null
        }
    }

    private fun specFunctions(): List<Pair<KFunction<*>, Spec?>> {
        return specificationClass.methods.filter {
            it.annotations.any { it is Test || it is Spec }
        }.map {
            val specAnnotation = it.annotations.filterIsInstance<Spec>().firstOrNull()
            it.kotlinFunction?.to(specAnnotation)
        }.filterNotNull()
    }

    private fun DescribeBody.buildBody(it: Pair<KFunction<*>, Spec?>, objectInstance: Any?) {
        val (function, spec) = it
        val invokeFunc: (DescribeBody) -> Unit = { body ->
            when (function.parameters.size) {
                0 -> function.call()
                1 -> function.call(if (function.parameters.first().kind == INSTANCE) objectInstance else body)
                2 -> function.call(objectInstance, body)
            }
        }
        val isDescribe = function.parameters.any { it.type.javaType == DescribeBody::class.java }
        val description = spec?.nullableName ?: function.name
        if (isDescribe) describe(description) {
            invokeFunc(this@describe)
        } else it(description) {
            invokeFunc(this)
        }
    }

    override fun getDescription(): Description? {
        return junitDescriptionCache.get(tree)
    }

    override fun run(junitNotifier: RunNotifier?) {
        tree.run(JUnitNotifier(junitNotifier!!, junitDescriptionCache))
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class Spec(val describeName: String = "")

private val Spec.nullableName: String?
    get() = if (describeName.isBlank()) null
    else describeName
