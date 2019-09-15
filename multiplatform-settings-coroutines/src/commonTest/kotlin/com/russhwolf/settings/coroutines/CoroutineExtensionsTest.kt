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

package com.russhwolf.settings.coroutines

import com.russhwolf.settings.ExperimentalListener
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalListener::class, ExperimentalCoroutinesApi::class)
abstract class CoroutineExtensionsTest {

    abstract val settings: ObservableSettings

    private fun <T> flowTest(
        flowBuilder: ObservableSettings.(String, T) -> Flow<T>,
        setter: Settings.(String, T) -> Unit,
        defaultValue: T,
        firstValue: T,
        secondValue: T
    ) = suspendTest {
        val flow = settings.flowBuilder("foo", defaultValue)

        settings.setter("foo", firstValue)
        val output = withTimeoutOrNull(1000) {
            flow
                .onStart {
                    launch {
                        delay(5)
                        settings.setter("foo", firstValue)
                        delay(5)
                        settings.setter("bar", firstValue)
                        delay(5)
                        settings.setter("foo", secondValue)
                        delay(5)
                        settings.remove("foo")
                    }
                }
                .take(3)
                .toList()
        }

        assertEquals(listOf(firstValue, secondValue, defaultValue), output)
    }

    private inline fun <reified T : Any> nullableFlowTest(
        crossinline flowBuilder: ObservableSettings.(String) -> Flow<T?>,
        crossinline setter: Settings.(String, T) -> Unit,
        firstValue: T,
        secondValue: T
    ) = flowTest(
        flowBuilder = { key, _ -> flowBuilder(key) },
        setter = { key, value -> if (value != null) setter(key, value) else remove(key) },
        defaultValue = null,
        firstValue = firstValue,
        secondValue = secondValue
    )

    @Test
    fun intFlowTest() = flowTest(
        flowBuilder = ObservableSettings::intFlow,
        setter = Settings::putInt,
        defaultValue = 0,
        firstValue = 3,
        secondValue = 8
    )

    @Test
    fun longFlowTest() = flowTest(
        flowBuilder = ObservableSettings::longFlow,
        setter = Settings::putLong,
        defaultValue = 0L,
        firstValue = 3L,
        secondValue = 8L
    )

    @Test
    fun stringFlowTest() = flowTest(
        flowBuilder = ObservableSettings::stringFlow,
        setter = Settings::putString,
        defaultValue = "",
        firstValue = "bar",
        secondValue = "baz"
    )

    @Test
    fun floatFlowTest() = flowTest(
        flowBuilder = ObservableSettings::floatFlow,
        setter = Settings::putFloat,
        defaultValue = 0f,
        firstValue = 3f,
        secondValue = 8f
    )

    @Test
    fun doubleFlowTest() = flowTest(
        flowBuilder = ObservableSettings::doubleFlow,
        setter = Settings::putDouble,
        defaultValue = 0.0,
        firstValue = 3.0,
        secondValue = 8.0
    )

    @Test
    fun booleanFlowTest() = flowTest(
        flowBuilder = ObservableSettings::booleanFlow,
        setter = Settings::putBoolean,
        defaultValue = false,
        firstValue = true,
        secondValue = false
    )

    @Test
    fun intOrNullFlowTest() = nullableFlowTest(
        flowBuilder = ObservableSettings::intOrNullFlow,
        setter = Settings::putInt,
        firstValue = 3,
        secondValue = 8
    )

    @Test
    fun longOrNullFlowTest() = nullableFlowTest(
        flowBuilder = ObservableSettings::longOrNullFlow,
        setter = Settings::putLong,
        firstValue = 3L,
        secondValue = 8L
    )

    @Test
    fun stringOrNullFlowTest() = nullableFlowTest(
        flowBuilder = ObservableSettings::stringOrNullFlow,
        setter = Settings::putString,
        firstValue = "bar",
        secondValue = "baz"
    )

    @Test
    fun floatOrNullFlowTest() = nullableFlowTest(
        flowBuilder = ObservableSettings::floatOrNullFlow,
        setter = Settings::putFloat,
        firstValue = 3f,
        secondValue = 8f
    )

    @Test
    fun doubleOrNullFlowTest() = nullableFlowTest(
        flowBuilder = ObservableSettings::doubleOrNullFlow,
        setter = Settings::putDouble,
        firstValue = 3.0,
        secondValue = 8.0
    )

    @Test
    fun booleanOrNullFlowTest() = nullableFlowTest(
        flowBuilder = ObservableSettings::booleanOrNullFlow,
        setter = Settings::putBoolean,
        firstValue = true,
        secondValue = false
    )
}
