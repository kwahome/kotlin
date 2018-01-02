// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
// MODULE: api
// FILE: api.kt

package api

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.SOURCE_ONLY)
@Target(AnnotationTarget.FUNCTION)
annotation class E1

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.SOURCE_ONLY)
@Target(AnnotationTarget.FUNCTION)
annotation class E2

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.SOURCE_ONLY)
@Target(AnnotationTarget.FUNCTION)
annotation class E3

@E1
fun e1() {}

@E2
fun e2() {}

@E3
fun e3() {}

// MODULE: usage(api)
// FILE: usage.kt

package usage

import api.*

@UseExperimental(E1::class, E2::class, E3::class)
fun use1() {
    e1()
    e2()
    e3()
}

@UseExperimental(E1::class, E3::class)
fun use2() {
    e1()
    @UseExperimental(E2::class) e2()
    e3()
}

@UseExperimental(E1::class, E2::class)
fun use3() {
    e1()
    e2()
    <!EXPERIMENTAL_API_USAGE!>e3<!>()
}
