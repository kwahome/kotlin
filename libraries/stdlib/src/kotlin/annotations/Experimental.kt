/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package kotlin

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

/**
 * Signals that the annotated annotation class is a marker of an experimental API. Any declaration annotated with that marker is thus
 * considered an experimental declaration and its call sites must accept the experimental aspect of it either by using [UseExperimental],
 * or by being annotated with that marker themselves, effectively causing further propagation of that experimental aspect.
 */
@Target(ANNOTATION_CLASS)
@Retention(BINARY)
annotation class Experimental(
        val level: ExperimentalLevel,
        val scope: ExperimentalScope
)

/**
 * Allows to use experimental API denoted by the given markers in the annotated file, declaration, or expression. Each of the given markers
 * must be an annotation class, annotated with [Experimental] with scope [ExperimentalScope.SOURCE_ONLY]. Any other given annotation classes
 * have no effect and are ignored.
 *
 * Only allows non-signature usages of the experimental API, i.e. inside a function body, variable initializer or default argument value.
 * (Usages in declaration signatures must be propagated by annotating the affected signature with the marker annotation itself.)
 */
@Target(CLASS, PROPERTY, LOCAL_VARIABLE, VALUE_PARAMETER, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, EXPRESSION, FILE)
@Retention(SOURCE)
annotation class UseExperimental(
        vararg val annotationClass: KClass<out Annotation>
)

/**
 * Severity of the diagnostic that should be reported on usages of experimental API which did not explicitly accept the experimental aspect
 * of that API either by using [UseExperimental] or by being annotated with the corresponding marker annotation.
 */
enum class ExperimentalLevel {
    /** Specifies that a warning should be reported on incorrect usages of this experimental API. */
    WARNING,
    /** Specifies that an error should be reported on incorrect usages of this experimental API. */
    ERROR,
}

/**
 * Scope of the experimental API controls the range where it's required to accept the experimental aspect of that API when using it
 * indirectly through other, non-experimental, declarations.
 */
enum class ExperimentalScope {
    /**
     * Non-signature usages (inside a function body, variable initializer or default argument value) of source-only experimental API
     * are allowed either if the containing declaration is annotated with the experimental annotation marker and thus propagates
     * the experimental aspect to its clients, or if there's a [UseExperimental] annotation entry with the corresponding annotation
     * marker somewhere above that usage in the parse tree. Signature usages of source-only experimental API always require propagation
     * (as long as the experimental API is declared in another module).
     */
    SOURCE_ONLY,
    /**
     * Any usage of a binary experimental API requires its containing declaration to be annotated with the corresponding annotation marker
     * (as long as the experimental API is declared in another module).
     */
    BINARY,
}
