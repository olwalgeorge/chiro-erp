package org.chiro.core_business_service.shared.domain.validation

/**
 * Represents the result of a validation operation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
) {
    companion object {
        fun success(): ValidationResult = ValidationResult(true, emptyList())
        
        fun failure(errors: List<ValidationError>): ValidationResult = 
            ValidationResult(false, errors)
            
        fun failure(error: ValidationError): ValidationResult = 
            ValidationResult(false, listOf(error))
    }
    
    fun hasErrors(): Boolean = errors.isNotEmpty()
    
    fun getErrorMessages(): List<String> = errors.map { it.message }
    
    fun merge(other: ValidationResult): ValidationResult {
        return ValidationResult(
            isValid = this.isValid && other.isValid,
            errors = this.errors + other.errors
        )
    }
}

/**
 * Represents a validation error
 */
data class ValidationError(
    val field: String,
    val message: String,
    val rejectedValue: String? = null,
    val code: String = "VALIDATION_ERROR"
) {
    override fun toString(): String {
        return "ValidationError(field='$field', message='$message', rejectedValue='$rejectedValue', code='$code')"
    }
}
