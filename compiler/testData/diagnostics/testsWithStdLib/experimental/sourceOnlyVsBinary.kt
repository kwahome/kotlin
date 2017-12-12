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

package usage1

import api.*

@UseExperimental(ExperimentalSourceOnlyAPI::class)
@ExperimentalBinaryAPI
fun use() {
    sourceOnly()
    binary()
}

@ExperimentalBinaryAPI
fun useUse() {
    use()
}


@ExperimentalSourceOnlyAPI
@ExperimentalBinaryAPI
fun recursiveUse() {
    sourceOnly()
    binary()
    recursiveUse()
}

// FILE: usage-no-annotation.txt

package usage2

import api.*

fun useUse() {
    usage1.<!EXPERIMENTAL_API_USAGE!>use<!>()
}
