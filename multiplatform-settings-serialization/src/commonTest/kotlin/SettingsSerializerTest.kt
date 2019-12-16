/*
 * Copyright 2019 Russell Wolf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.russhwolf.settings.serialization

import com.russhwolf.settings.MockSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsSerializerTest {

    @Test
    fun serialize() {
        val foo = Foo("hello", 43110)

        val settings: Settings = MockSettings()
        val settingsFormat = settings.serialFormat()

        settingsFormat.serializeToSettings(Foo.serializer(), "foo", foo)

        assertEquals("hello", settings.getString("foo.bar"))
        assertEquals(43110, settings.getInt("foo.baz"))
    }

    @Test
    fun deserialize() {
        val settings: Settings = MockSettings("foo.bar" to "hello", "foo.baz" to 43110)
        val settingsFormat = settings.serialFormat()

        val foo = settingsFormat.deserializeFromSettings(Foo.serializer(), "foo")

        assertEquals("hello", foo.bar)
        assertEquals(43110, foo.baz)
    }

    @Test
    fun deserialize_empty() {
        val settings: Settings = MockSettings()
        val settingsFormat = settings.serialFormat()

        val foo = settingsFormat.deserializeFromSettings(Foo.serializer(), "foo")

        assertEquals("", foo.bar)
        assertEquals(0, foo.baz)
    }

    @Test
    fun delegate() {
        val settings: Settings = MockSettings()
        val delegate = settings.serialDelegate(Foo.serializer(), "foo")
        var foo: Foo by delegate
        assertEquals(Foo("", 0), foo)

        foo = Foo("hello", 43110)

        assertEquals("hello", settings.getString("foo.bar"))
        assertEquals(43110, settings.getInt("foo.baz"))

        foo = Foo("hi", 41)

        assertEquals("hi", settings.getString("foo.bar"))
        assertEquals(41, settings.getInt("foo.baz"))

        val foo2: Foo by delegate
        assertEquals(foo, foo2)
    }

    @Test
    fun delegate_keyless() {
        val settings: Settings = MockSettings()
        val delegate = settings.serialDelegate(Foo.serializer())
        var foo: Foo by delegate
        assertEquals(Foo("", 0), foo)

        foo = Foo("hello", 43110)

        assertEquals("hello", settings.getString("foo.bar"))
        assertEquals(43110, settings.getInt("foo.baz"))

        foo = Foo("hi", 41)

        assertEquals("hi", settings.getString("foo.bar"))
        assertEquals(41, settings.getInt("foo.baz"))

        val foo2: Foo by delegate
        assertEquals(Foo("", 0), foo2)
    }

    @Test
    fun allTypes() {
        val settings: Settings = MockSettings()
        val testClass = TestClass(
            boolean = true,
            byte = 1,
            char = '2',
            double = 3.0,
            enum = TestEnum.A,
            float = 4f,
            int = 5,
            long = 6L,
            short = 7,
            string = "8",
//            unit = Unit,
            nested = TestClass()
        )

        settings.serialFormat().serializeToSettings(TestClass.serializer(), "testClass", testClass)

        assertEquals(true, settings.getBoolean("testClass.boolean"))
        assertTrue(settings.getBoolean("testClass.boolean?"))
        assertEquals(1, settings.getInt("testClass.byte"))
        assertTrue(settings.getBoolean("testClass.byte?"))
        assertEquals('2'.toInt(), settings.getInt("testClass.char"))
        assertTrue(settings.getBoolean("testClass.char?"))
        assertEquals(3.0, settings.getDouble("testClass.double"))
        assertTrue(settings.getBoolean("testClass.double?"))
        assertEquals(TestEnum.A.ordinal, settings.getInt("testClass.enum"))
        assertTrue(settings.getBoolean("testClass.enum?"))
        assertEquals(4f, settings.getFloat("testClass.float"))
        assertTrue(settings.getBoolean("testClass.float?"))
        assertEquals(5, settings.getInt("testClass.int"))
        assertTrue(settings.getBoolean("testClass.int?"))
        assertEquals(6L, settings.getLong("testClass.long"))
        assertTrue(settings.getBoolean("testClass.long?"))
        assertEquals(7, settings.getInt("testClass.short"))
        assertTrue(settings.getBoolean("testClass.short?"))
        assertEquals("8", settings.getString("testClass.string"))
        assertTrue(settings.getBoolean("testClass.string?"))
////        assertEquals(true, settings.getBoolean("testClass.unit"))
//        assertTrue(settings.getBoolean("testClass.unit?"))

        assertFalse("testClass.nested.boolean" in settings)
        assertFalse(settings.getBoolean("testClass.nested.boolean?", true))
        assertFalse("testClass.nested.byte" in settings)
        assertFalse(settings.getBoolean("testClass.nested.byte?", true))
        assertFalse("testClass.nested.char" in settings)
        assertFalse(settings.getBoolean("testClass.nested.char?", true))
        assertFalse("testClass.nested.double" in settings)
        assertFalse(settings.getBoolean("testClass.nested.double?", true))
        assertFalse("testClass.nested.enum" in settings)
        assertFalse(settings.getBoolean("testClass.nested.enum?", true))
        assertFalse("testClass.nested.float" in settings)
        assertFalse(settings.getBoolean("testClass.nested.float?", true))
        assertFalse("testClass.nested.int" in settings)
        assertFalse(settings.getBoolean("testClass.nested.int?", true))
        assertFalse("testClass.nested.long" in settings)
        assertFalse(settings.getBoolean("testClass.nested.long?", true))
        assertFalse("testClass.nested.short" in settings)
        assertFalse(settings.getBoolean("testClass.nested.short?", true))
        assertFalse("testClass.nested.string" in settings)
        assertFalse(settings.getBoolean("testClass.nested.string?", true))
////        assertFalse("testClass.nested.unit" in settings)
//        assertFalse(settings.getBoolean("testClass.nested.unit?", true))

        assertEquals(testClass, settings.serialFormat().deserializeFromSettings(TestClass.serializer(), "testClass"))
    }
}

@Serializable
data class Foo(val bar: String, val baz: Int)

@Serializable
data class TestClass(
    val boolean: Boolean? = null,
    val byte: Byte? = null,
    val char: Char? = null,
    val double: Double? = null,
    val enum: TestEnum? = null,
    val float: Float? = null,
    val int: Int? = null,
    val long: Long? = null,
    val short: Short? = null,
    val string: String? = null,
//    val unit: Unit = Unit,
    val nested: TestClass? = null
)

@Serializable
enum class TestEnum { A, B }
