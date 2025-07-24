package org.chiro.core_business_service.shared.domain.valueobject

import jakarta.persistence.Embeddable
import jakarta.persistence.Column
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

/**
 * Contact Information Value Object - shared across all ERP modules.
 * 
 * Used for customer, vendor, employee, and supplier contact details.
 * Ensures consistent contact data representation across the entire system.
 * 
 * Business Rules:
 * - Email must be valid format
 * - Phone numbers support international formats
 * - Address components have appropriate size limits
 * - All fields are optional but when provided must be valid
 */
@Embeddable
data class ContactInfo(
    
    @field:Email(message = "Email must be valid")
    @field:Size(max = 254, message = "Email cannot exceed 254 characters")
    @Column(name = "email", length = 254)
    val email: String? = null,
    
    @field:Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Column(name = "phone", length = 20)
    val phone: String? = null,
    
    @field:Size(max = 20, message = "Mobile number cannot exceed 20 characters")
    @Column(name = "mobile", length = 20)
    val mobile: String? = null,
    
    @field:Size(max = 20, message = "Fax number cannot exceed 20 characters")
    @Column(name = "fax", length = 20)
    val fax: String? = null,
    
    @field:Size(max = 200, message = "Website cannot exceed 200 characters")
    @Column(name = "website", length = 200)
    val website: String? = null

) {
    
    companion object {
        
        /**
         * Create empty contact info
         */
        fun empty(): ContactInfo = ContactInfo()
        
        /**
         * Create contact info with just email
         */
        fun withEmail(email: String): ContactInfo = ContactInfo(email = email)
        
        /**
         * Create contact info with email and phone
         */
        fun withEmailAndPhone(email: String, phone: String): ContactInfo = 
            ContactInfo(email = email, phone = phone)
    }
    
    /**
     * Check if contact info has any non-null values
     */
    fun hasAnyContact(): Boolean {
        return email != null || phone != null || mobile != null || fax != null || website != null
    }
    
    /**
     * Check if contact info has email
     */
    fun hasEmail(): Boolean = !email.isNullOrBlank()
    
    /**
     * Check if contact info has phone
     */
    fun hasPhone(): Boolean = !phone.isNullOrBlank()
    
    /**
     * Get primary contact method (email preferred, then phone, then mobile)
     */
    fun getPrimaryContact(): String? {
        return when {
            hasEmail() -> email
            hasPhone() -> phone
            !mobile.isNullOrBlank() -> mobile
            else -> null
        }
    }
    
    /**
     * Validate contact information
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        // Email validation
        email?.let { emailVal ->
            if (emailVal.isBlank()) {
                errors.add("Email cannot be blank when provided")
            } else if (!isValidEmail(emailVal)) {
                errors.add("Email format is invalid: $emailVal")
            }
        }
        
        // Phone validation
        phone?.let { phoneVal ->
            if (phoneVal.isBlank()) {
                errors.add("Phone cannot be blank when provided")
            } else if (!isValidPhoneNumber(phoneVal)) {
                errors.add("Phone number format is invalid: $phoneVal")
            }
        }
        
        // Mobile validation
        mobile?.let { mobileVal ->
            if (mobileVal.isBlank()) {
                errors.add("Mobile cannot be blank when provided")
            } else if (!isValidPhoneNumber(mobileVal)) {
                errors.add("Mobile number format is invalid: $mobileVal")
            }
        }
        
        return errors
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }
    
    private fun isValidPhoneNumber(phone: String): Boolean {
        // Support international phone numbers with optional + prefix
        val phoneRegex = "^\\+?[1-9]\\d{6,19}$".toRegex()
        return phoneRegex.matches(phone.replace("\\s|-|\\(|\\)".toRegex(), ""))
    }
}
