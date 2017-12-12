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

package org.jetbrains.kotlin.resolve.checkers

import com.intellij.psi.PsiCompiledElement
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getElementTextWithContext
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.utils.SmartSet
import org.jetbrains.kotlin.utils.addIfNotNull

object ExperimentalUsageChecker : CallChecker {
    private val EXPERIMENTAL_FQ_NAME = FqName("kotlin.Experimental")
    private val USE_EXPERIMENTAL_FQ_NAME = FqName("kotlin.UseExperimental")
    private val USE_EXPERIMENTAL_ANNOTATION_CLASS = Name.identifier("annotationClass")

    private val LEVEL = Name.identifier("level")
    private val WARNING_LEVEL = Name.identifier("WARNING")
    private val ERROR_LEVEL = Name.identifier("ERROR")

    private val SCOPE = Name.identifier("scope")
    private val SOURCE_ONLY_SCOPE = Name.identifier("SOURCE_ONLY")
    private val BINARY_SCOPE = Name.identifier("BINARY")

    private data class Experimentality(val annotationFqName: FqName, val severity: Severity, val scope: Scope) {
        enum class Severity { WARNING, ERROR }
        enum class Scope { SOURCE_ONLY, BINARY }
    }

    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        // TODO: ensure reportOn is never a synthetic element
        checkExperimental(resolvedCall.resultingDescriptor, reportOn, context.trace)
    }

    private fun checkExperimental(descriptor: DeclarationDescriptor, element: PsiElement, trace: BindingTrace) {
        assert(element !is PsiCompiledElement) {
            "Checkers should only be run on source PSI elements: ${element.getElementTextWithContext()}"
        }

        for ((annotationFqName, severity, scope) in descriptor.loadExperimentalities()) {
            val isBodyUsageOfSourceOnlyExperimentality =
                    scope == Experimentality.Scope.SOURCE_ONLY && element.isBodyUsage()

            val isExperimentalityAccepted =
                    (isBodyUsageOfSourceOnlyExperimentality &&
                     element.hasContainerAnnotatedWithUseExperimental(annotationFqName, trace.bindingContext)) ||
                    element.propagates(annotationFqName, trace.bindingContext)

            if (!isExperimentalityAccepted) {
                val diagnostic = when (severity) {
                    ExperimentalUsageChecker.Experimentality.Severity.WARNING -> Errors.EXPERIMENTAL_API_USAGE
                    ExperimentalUsageChecker.Experimentality.Severity.ERROR -> Errors.EXPERIMENTAL_API_USAGE_ERROR
                }
                trace.report(diagnostic.on(element, annotationFqName, isBodyUsageOfSourceOnlyExperimentality))
            }
        }
    }

    private fun DeclarationDescriptor.loadExperimentalities(): Set<Experimentality> {
        val result = SmartSet.create<Experimentality>()

        for (annotation in annotations) {
            result.addIfNotNull(annotation.loadExperimentalityForMarkerAnnotation())
        }

        val container = containingDeclaration
        if (container is ClassDescriptor && this !is ConstructorDescriptor) {
            for (annotation in container.annotations) {
                result.addIfNotNull(annotation.loadExperimentalityForMarkerAnnotation())
            }
        }

        return result
    }

    private fun AnnotationDescriptor.loadExperimentalityForMarkerAnnotation(): Experimentality? {
        val experimental = annotationClass?.annotations?.findAnnotation(EXPERIMENTAL_FQ_NAME) ?: return null
        val annotationFqName = fqName ?: return null

        val severity = when ((experimental.allValueArguments[LEVEL] as? EnumValue)?.value?.name) {
            WARNING_LEVEL -> Experimentality.Severity.WARNING
            ERROR_LEVEL -> Experimentality.Severity.ERROR
            else -> return null
        }

        val scope = when ((experimental.allValueArguments[SCOPE] as? EnumValue)?.value?.name) {
            SOURCE_ONLY_SCOPE -> Experimentality.Scope.SOURCE_ONLY
            BINARY_SCOPE -> Experimentality.Scope.BINARY
            else -> return null
        }

        return Experimentality(annotationFqName, severity, scope)
    }

    // Returns true if this element appears in the body of some function and is not visible in any non-local declaration signature.
    // If that's the case, one can opt-in to using the corresponding experimental API by annotating this element (or any of its
    // enclosing declarations) with @UseExperimental(X::class), not requiring propagation of the experimental annotation to the call sites.
    // (Note that this is allowed only if X's scope is SOURCE_ONLY.)
    private fun PsiElement.isBodyUsage(): Boolean {
        var element = this
        while (true) {
            val parent = element.parent ?: return false

            if (element == (parent as? KtDeclarationWithBody)?.bodyExpression ||
                element == (parent as? KtDeclarationWithInitializer)?.initializer ||
                element == (parent as? KtClassInitializer)?.body ||
                element == (parent as? KtParameter)?.defaultValue ||
                element == (parent as? KtSuperTypeCallEntry)?.valueArgumentList ||
                element == (parent as? KtDelegatedSuperTypeEntry)?.delegateExpression ||
                element == (parent as? KtPropertyDelegate)?.expression) return true

            if (element is KtFile) return false
            element = parent
        }
    }

    // Checks whether any of the non-local enclosing declarations is annotated with annotationFqName, effectively requiring
    // propagation for the experimental annotation to the call sites
    private fun PsiElement.propagates(annotationFqName: FqName, bindingContext: BindingContext): Boolean {
        var element = this
        while (true) {
            if (element is KtDeclaration) {
                val descriptor = bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, element)
                if (descriptor != null && !DescriptorUtils.isLocal(descriptor) &&
                    descriptor.annotations.hasAnnotation(annotationFqName)) return true
            }

            if (element is KtFile) return false
            element = element.parent ?: return false
        }
    }

    // Checks whether there's an element lexically above the tree, that is annotated with `@UseExperimental(X::class)`
    // where annotationFqName is the FQ name of X
    private fun PsiElement.hasContainerAnnotatedWithUseExperimental(annotationFqName: FqName, bindingContext: BindingContext): Boolean {
        var element = this
        while (true) {
            if (element is KtAnnotated && element.annotationEntries.any { entry ->
                bindingContext.get(BindingContext.ANNOTATION, entry)?.isUseExperimental(annotationFqName) == true
            }) return true

            if (element is KtFile) return false
            element = element.parent ?: return false
        }
    }

    private fun AnnotationDescriptor.isUseExperimental(annotationFqName: FqName): Boolean {
        if (fqName != USE_EXPERIMENTAL_FQ_NAME) return false

        val annotationClasses = allValueArguments[USE_EXPERIMENTAL_ANNOTATION_CLASS]
        return annotationClasses is ArrayValue && annotationClasses.value.any { annotationClass ->
            (annotationClass as? KClassValue)?.value?.constructor?.declarationDescriptor?.fqNameSafe == annotationFqName
        }
    }

    object ClassifierUsage : ClassifierUsageChecker {
        override fun check(targetDescriptor: ClassifierDescriptor, element: PsiElement, context: ClassifierUsageCheckerContext) {
            checkExperimental(targetDescriptor, element, context.trace)
        }
    }
}
