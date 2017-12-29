/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.resolve.constants

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils

object ConstantValueFactory {
    fun createArrayValue(value: List<ConstantValue<*>>, type: KotlinType) = createArrayValue(value) { type }

    fun createConstantValue(value: Any?): ConstantValue<*>? {
        return when (value) {
            is Byte -> ByteValue(value)
            is Short -> ShortValue(value)
            is Int -> IntValue(value)
            is Long -> LongValue(value)
            is Char -> CharValue(value)
            is Float -> FloatValue(value)
            is Double -> DoubleValue(value)
            is Boolean -> BooleanValue(value)
            is String -> StringValue(value)
            is ByteArray -> createArrayValue(value.toList().arrayToList(), PrimitiveType.BYTE.arrayType())
            is ShortArray -> createArrayValue(value.toList().arrayToList(), PrimitiveType.SHORT.arrayType())
            is IntArray -> createArrayValue(value.toList().arrayToList(), PrimitiveType.INT.arrayType())
            is LongArray -> createArrayValue(value.toList().arrayToList(), PrimitiveType.LONG.arrayType())
            is CharArray -> createArrayValue(value.toList().arrayToList(), PrimitiveType.CHAR.arrayType())
            is FloatArray -> createArrayValue(value.toList().arrayToList(), PrimitiveType.FLOAT.arrayType())
            is DoubleArray -> createArrayValue(value.toList().arrayToList(), PrimitiveType.DOUBLE.arrayType())
            is BooleanArray -> createArrayValue(value.toList().arrayToList(), PrimitiveType.BOOLEAN.arrayType())
            null -> NullValue()
            else -> null
        }
    }

    private fun createArrayValue(value: List<ConstantValue<*>>, computeType: (ModuleDescriptor) -> KotlinType): ArrayValue =
            ArrayValue(value, computeType)

    private fun List<*>.arrayToList(): List<ConstantValue<*>> =
            this.toList().mapNotNull { createConstantValue(it) }

    private fun PrimitiveType.arrayType(): (ModuleDescriptor) -> KotlinType =
            { module -> module.builtIns.getPrimitiveArrayKotlinType(this) }

    fun createIntegerConstantValue(
            value: Long,
            expectedType: KotlinType
    ): ConstantValue<*>? {
        val notNullExpected = TypeUtils.makeNotNullable(expectedType)
        return when {
            KotlinBuiltIns.isLong(notNullExpected) -> LongValue(value)
            KotlinBuiltIns.isInt(notNullExpected) && value == value.toInt().toLong() -> IntValue(value.toInt())
            KotlinBuiltIns.isShort(notNullExpected) && value == value.toShort().toLong() -> ShortValue(value.toShort())
            KotlinBuiltIns.isByte(notNullExpected) && value == value.toByte().toLong() -> ByteValue(value.toByte())
            KotlinBuiltIns.isChar(notNullExpected) -> IntValue(value.toInt())
            else -> null
        }
    }
}
