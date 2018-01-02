// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
// MODULE: api
// FILE: api.kt

package api

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.SOURCE_ONLY)
annotation class ExperimentalSourceOnlyAPI

interface I

@ExperimentalSourceOnlyAPI
class Impl : I

// MODULE: usage(api)
// FILE: usage.kt

package usage

import api.*

open class Base(i: I)

@UseExperimental(ExperimentalSourceOnlyAPI::class)
class Derived : Base(Impl())

@UseExperimental(ExperimentalSourceOnlyAPI::class)
class Delegated : I by Impl()

@UseExperimental(ExperimentalSourceOnlyAPI::class)
val delegatedProperty by Impl()
operator fun I.getValue(x: Any?, y: Any?) = null
