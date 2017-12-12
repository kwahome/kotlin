// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
// FILE: api.kt

package api

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.SOURCE_ONLY)
@Target(AnnotationTarget.FUNCTION)
annotation class SourceOnlyExperimentalAPI

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class BinaryExperimentalAPI

@SourceOnlyExperimentalAPI
fun sourceOnly() {}

@BinaryExperimentalAPI
fun binary() {}

// FILE: usage.kt

@file:UseExperimental(SourceOnlyExperimentalAPI::class)
package usage

import api.*

fun use() {
    sourceOnly()
    <!EXPERIMENTAL_API_USAGE!>binary<!>()
}

class Use {
    fun use() {
        sourceOnly()
        <!EXPERIMENTAL_API_USAGE!>binary<!>()
    }
}
