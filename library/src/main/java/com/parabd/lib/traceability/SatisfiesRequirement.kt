package com.parabd.lib.traceability

/**
 * Annotation used to mark code elements that satisfy specific functional or architectural requirements.
 *
 * @property id The requirement ID satisfied by the annotated element (e.g., "6.1.2.9").
 */
@SatisfiesRequirement("6.1.2.9")
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class SatisfiesRequirement(val id: String)
