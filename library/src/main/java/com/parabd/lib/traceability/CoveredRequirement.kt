package com.parabd.lib.traceability

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class CoveredRequirement(val id: String)

