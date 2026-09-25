package com.parabd.lib.traceability

/**
 * Annotation used to document requirement coverage in test cases or implementation code.
 *
 * @property id The requirement ID covered by the annotated element (e.g., "6.1.2.9.1").
 */
@SatisfiesRequirement("6.1.2.9")
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class CoveredRequirement(val id: String)
