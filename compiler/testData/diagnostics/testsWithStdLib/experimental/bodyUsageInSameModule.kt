// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
// FILE: api.kt

package api

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.SOURCE_ONLY)
annotation class ExperimentalSourceOnlyAPI

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.BINARY)
annotation class ExperimentalBinaryAPI

@ExperimentalSourceOnlyAPI
fun sourceOnly() {}

@ExperimentalBinaryAPI
fun binary() {}

// FILE: usage.kt

package usage

import api.*

fun use1() {
    sourceOnly()
    binary()
}

val use2 = sourceOnly()
val use3 = binary()
