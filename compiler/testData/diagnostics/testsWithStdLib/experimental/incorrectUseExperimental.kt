// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
// MODULE: api
// FILE: api.kt

package api

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.BINARY)
@Target(AnnotationTarget.PROPERTY)
annotation class BinaryExperimental

@BinaryExperimental
val x = ""

// MODULE: usage(api)
// FILE: usage.kt

import api.*

<!USE_EXPERIMENTAL_WITHOUT_ARGUMENTS!>@UseExperimental<!>
fun use1(): String {
    return <!EXPERIMENTAL_API_USAGE!>x<!>
}

<!USE_EXPERIMENTAL_ARGUMENT_HAS_BINARY_SCOPE!>@UseExperimental(BinaryExperimental::class)<!>
fun use2(): String {
    return <!EXPERIMENTAL_API_USAGE!>x<!>
}

<!USE_EXPERIMENTAL_ARGUMENT_IS_NOT_MARKER!>@UseExperimental(UseExperimental::class)<!>
fun use3(): String {
    return <!EXPERIMENTAL_API_USAGE!>x<!>
}
