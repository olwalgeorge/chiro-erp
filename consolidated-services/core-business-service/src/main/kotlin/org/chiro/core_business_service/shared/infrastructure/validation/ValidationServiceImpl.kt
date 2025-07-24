package org.chiro.core_business_service.shared.infrastructure.validation

import org.chiro.core_business_service.shared.domain.validation.ValidationResult
import org.chiro.core_business_service.shared.domain.validation.ValidationError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.validation.Validator
import jakarta.validation.ConstraintViolation
import jakarta.inject.Inject
import java.util.regex.Pattern

/**
 * Validation service implementation providing shared business rule validation.
 * 
 * This service offers:
 * - Bean validation integration
 * - Custom business rule validation
 * - Cross-field validation logic
 * - Internationalized error messages
 * - Validation result aggregation
 */
@ApplicationScoped
class ValidationServiceImpl : ValidationService {

    private val logger: Logger = LoggerFactory.getLogger(ValidationServiceImpl::class.java)

    @Inject
    lateinit var validator: Validator

    // Common validation patterns
    private val emailPattern = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )
    
    private val phonePattern = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    )
    
    private val alphanumericPattern = Pattern.compile(
        "^[a-zA-Z0-9]+$"
    )

    /**
     * Validate an object using Bean Validation annotations.
     * 
     * @param obj The object to validate
     * @return Validation result
     */
    override fun <T> validate(obj: T): ValidationResult {
        return try {
            val violations = validator.validate(obj)
            
            if (violations.isEmpty()) {
                ValidationResult.success()
            } else {
                val errors = violations.map { violation ->
                    ValidationError(
                        field = violation.propertyPath.toString(),
                        message = violation.message,
                        rejectedValue = violation.invalidValue?.toString(),
                        code = violation.constraintDescriptor.annotation.annotationClass.simpleName ?: "Unknown"
                    )
                }
                
                ValidationResult.failure(errors)
            }
            
        } catch (e: Exception) {
            logger.error("Validation failed for object: ${obj?.let { it::class.simpleName }}", e)
            ValidationResult.failure(
                listOf(
                    ValidationError(
                        field = "general",
                        message = "Validation process failed: ${e.message}",
                        code = "VALIDATION_ERROR"
                    )
                )
            )
        }
    }

    /**
     * Validate multiple objects and aggregate results.
     * 
     * @param objects The objects to validate
     * @return Aggregated validation result
     */
    override fun validateAll(vararg objects: Any): ValidationResult {
        val allErrors = mutableListOf<ValidationError>()

        objects.forEachIndexed { index, obj ->
            val result = validate(obj)
            if (!result.isValid) {
                // Prefix field names with object index for clarity
                val prefixedErrors = result.errors.map { error ->
                    error.copy(field = "object[$index].${error.field}")
                }
                allErrors.addAll(prefixedErrors)
            }
        }

        return if (allErrors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(allErrors)
        }
    }

    /**
     * Validate a string as an email address.
     * 
     * @param email The email to validate
     * @param fieldName The field name for error reporting
     * @return Validation result
     */
    override fun validateEmail(email: String?, fieldName: String): ValidationResult {
        return when {
            email.isNullOrBlank() -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Email is required", email, "REQUIRED"))
            )
            
            !emailPattern.matcher(email).matches() -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Invalid email format", email, "INVALID_EMAIL"))
            )
            
            email.length > 254 -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Email is too long (max 254 characters)", email, "TOO_LONG"))
            )
            
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate a string as a phone number.
     * 
     * @param phone The phone number to validate
     * @param fieldName The field name for error reporting
     * @return Validation result
     */
    override fun validatePhoneNumber(phone: String?, fieldName: String): ValidationResult {
        return when {
            phone.isNullOrBlank() -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Phone number is required", phone, "REQUIRED"))
            )
            
            !phonePattern.matcher(phone).matches() -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Invalid phone number format", phone, "INVALID_PHONE"))
            )
            
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate a string contains only alphanumeric characters.
     * 
     * @param value The value to validate
     * @param fieldName The field name for error reporting
     * @return Validation result
     */
    override fun validateAlphanumeric(value: String?, fieldName: String): ValidationResult {
        return when {
            value.isNullOrBlank() -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Value is required", value, "REQUIRED"))
            )
            
            !alphanumericPattern.matcher(value).matches() -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Value must contain only alphanumeric characters", value, "INVALID_FORMAT"))
            )
            
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate a string length is within specified bounds.
     * 
     * @param value The value to validate
     * @param fieldName The field name for error reporting
     * @param minLength Minimum length (inclusive)
     * @param maxLength Maximum length (inclusive)
     * @return Validation result
     */
    override fun validateLength(
        value: String?,
        fieldName: String,
        minLength: Int,
        maxLength: Int
    ): ValidationResult {
        val length = value?.length ?: 0
        
        return when {
            value.isNullOrBlank() && minLength > 0 -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Value is required", value, "REQUIRED"))
            )
            
            length < minLength -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Value is too short (minimum $minLength characters)", value, "TOO_SHORT"))
            )
            
            length > maxLength -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Value is too long (maximum $maxLength characters)", value, "TOO_LONG"))
            )
            
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate a numeric value is within specified range.
     * 
     * @param value The value to validate
     * @param fieldName The field name for error reporting
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return Validation result
     */
    override fun validateRange(
        value: Number?,
        fieldName: String,
        min: Double,
        max: Double
    ): ValidationResult {
        return when {
            value == null -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Value is required", null, "REQUIRED"))
            )
            
            value.toDouble() < min -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Value is below minimum ($min)", value.toString(), "BELOW_MIN"))
            )
            
            value.toDouble() > max -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Value is above maximum ($max)", value.toString(), "ABOVE_MAX"))
            )
            
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate that a value is not null or empty.
     * 
     * @param value The value to validate
     * @param fieldName The field name for error reporting
     * @return Validation result
     */
    override fun validateRequired(value: Any?, fieldName: String): ValidationResult {
        return when {
            value == null -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Value is required", null, "REQUIRED"))
            )
            
            value is String && value.isBlank() -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Value cannot be blank", value, "BLANK"))
            )
            
            value is Collection<*> && value.isEmpty() -> ValidationResult.failure(
                listOf(ValidationError(fieldName, "Collection cannot be empty", "[]", "EMPTY"))
            )
            
            else -> ValidationResult.success()
        }
    }

    /**
     * Validate using a custom validation function.
     * 
     * @param value The value to validate
     * @param fieldName The field name for error reporting
     * @param validator Custom validation function
     * @return Validation result
     */
    override fun <T> validateCustom(
        value: T,
        fieldName: String,
        validator: (T) -> ValidationResult
    ): ValidationResult {
        return try {
            validator(value)
        } catch (e: Exception) {
            logger.error("Custom validation failed for field: $fieldName", e)
            ValidationResult.failure(
                listOf(
                    ValidationError(
                        fieldName,
                        "Custom validation failed: ${e.message}",
                        value?.toString(),
                        "CUSTOM_VALIDATION_ERROR"
                    )
                )
            )
        }
    }

    /**
     * Combine multiple validation results into a single result.
     * 
     * @param results The validation results to combine
     * @return Combined validation result
     */
    override fun combineResults(vararg results: ValidationResult): ValidationResult {
        val allErrors = results.flatMap { it.errors }
        
        return if (allErrors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(allErrors)
        }
    }
}

/**
 * Validation service interface.
 */
interface ValidationService {
    fun <T> validate(obj: T): ValidationResult
    fun validateAll(vararg objects: Any): ValidationResult
    fun validateEmail(email: String?, fieldName: String): ValidationResult
    fun validatePhoneNumber(phone: String?, fieldName: String): ValidationResult
    fun validateAlphanumeric(value: String?, fieldName: String): ValidationResult
    fun validateLength(value: String?, fieldName: String, minLength: Int, maxLength: Int): ValidationResult
    fun validateRange(value: Number?, fieldName: String, min: Double, max: Double): ValidationResult
    fun validateRequired(value: Any?, fieldName: String): ValidationResult
    fun <T> validateCustom(value: T, fieldName: String, validator: (T) -> ValidationResult): ValidationResult
    fun combineResults(vararg results: ValidationResult): ValidationResult
}
