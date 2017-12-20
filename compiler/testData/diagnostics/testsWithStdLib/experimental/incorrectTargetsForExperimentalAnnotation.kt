// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
// FILE: api.kt

package api

import kotlin.annotation.AnnotationTarget.*

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.BINARY)
@Target(CLASS, ANNOTATION_CLASS, TYPE_PARAMETER, PROPERTY, FIELD, LOCAL_VARIABLE, VALUE_PARAMETER, CONSTRUCTOR, FUNCTION,
        PROPERTY_GETTER, PROPERTY_SETTER, TYPE, TYPEALIAS)
annotation class E1

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.BINARY)
<!EXPERIMENTAL_ANNOTATION_WITH_WRONG_TARGET!>@Target(FILE)<!>
annotation class E2

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.BINARY)
<!EXPERIMENTAL_ANNOTATION_WITH_WRONG_TARGET!>@Target(EXPRESSION)<!>
annotation class E3

@Experimental(ExperimentalLevel.WARNING, ExperimentalScope.BINARY)
<!EXPERIMENTAL_ANNOTATION_WITH_WRONG_TARGET!>@Target(FILE, EXPRESSION)<!>
annotation class E4
