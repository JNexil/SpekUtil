@file:JvmName("ExampleTest")

//Main description in test-trace is JVM class name

import org.jetbrains.spek.api.DescribeBody
import org.junit.Test
import org.junit.runner.RunWith
import su.jfdev.spek.hybrid.Spec
import su.jfdev.spek.hybrid.SpekHybrid
import su.jfdev.spek.hybrid.SpekLink
import su.jfdev.spek.wrapper.DescribeTitle.*
import su.jfdev.spek.wrapper.invoke

class `LOL and lol`: SpekLink()

//@Spec maybe used for setting custom name
@Test @Spec("should same this") fun DescribeBody.`should this`() {
    it("LOL") {
        assert(true)
    }
}

//@Test maybe used without @Spec
@Test fun DescribeBody.`should other`() {
    it("LOL") {
        assert(true)
    }
}

//Notify: Without @Test, it's invisible in IDEA
@Test fun `should other`() {
    assert(true)
}

@RunWith(SpekHybrid::class)
object NestedTest {
    @Test fun DescribeBody.`should this in object`() {
        it("LOL") {
            assert(true)
        }
    }

    @Test fun `should assert in object`() {
        assert(true)
    }
}