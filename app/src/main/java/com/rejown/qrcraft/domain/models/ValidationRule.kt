package com.rejown.qrcraft.domain.models

/**
 * Validation rules for form fields
 */
sealed class ValidationRule {
    object Email : ValidationRule()
    object Phone : ValidationRule()
    object Url : ValidationRule()
    data class MinLength(val min: Int) : ValidationRule()
    data class MaxLength(val max: Int) : ValidationRule()
    data class Pattern(val regex: Regex, val message: String) : ValidationRule()
    data class Required(val message: String = "This field is required") : ValidationRule()
}
