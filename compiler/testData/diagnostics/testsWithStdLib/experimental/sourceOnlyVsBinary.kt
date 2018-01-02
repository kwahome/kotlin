// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
// MODULE: api
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

// MODULE: usage1(api)
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

// MODULE: usage2(api,usage1)
// FILE: usage-no-annotation.txt

package usage2

import api.*

fun useUse() {
    usage1.<!EXPERIMENTAL_API_USAGE!>use<!>()
}
