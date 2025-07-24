package org.chiro.core_business_service.shared.domain.valueobject

import jakarta.persistence.Embeddable
import jakarta.persistence.Column
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Address Value Object - shared across all ERP modules.
 * 
 * Used for customer addresses, vendor addresses, shipping addresses, 
 * billing addresses, warehouse locations, etc.
 * 
 * Provides standardized address representation with international support.
 * 
 * Business Rules:
 * - Street address is required
 * - City and country are required
 * - State/Province required for countries that have states
 * - Postal code format varies by country
 * - All text fields have appropriate length limits
 */
@Embeddable
data class Address(
    
    @field:NotBlank(message = "Street address is required")
    @field:Size(max = 255, message = "Street address cannot exceed 255 characters")
    @Column(name = "street_address", length = 255, nullable = false)
    val streetAddress: String,
    
    @field:Size(max = 100, message = "Street address line 2 cannot exceed 100 characters")
    @Column(name = "street_address_2", length = 100)
    val streetAddress2: String? = null,
    
    @field:NotBlank(message = "City is required")
    @field:Size(max = 100, message = "City cannot exceed 100 characters")
    @Column(name = "city", length = 100, nullable = false)
    val city: String,
    
    @field:Size(max = 100, message = "State/Province cannot exceed 100 characters")
    @Column(name = "state_province", length = 100)
    val stateProvince: String? = null,
    
    @field:Size(max = 20, message = "Postal code cannot exceed 20 characters")
    @Column(name = "postal_code", length = 20)
    val postalCode: String? = null,
    
    @field:NotBlank(message = "Country is required")
    @field:Size(max = 2, message = "Country code must be 2 characters (ISO 3166-1 alpha-2)")
    @Column(name = "country_code", length = 2, nullable = false)
    val countryCode: String

) {
    
    companion object {
        
        /**
         * Create a simple address with required fields only
         */
        fun simple(streetAddress: String, city: String, countryCode: String): Address {
            return Address(
                streetAddress = streetAddress,
                city = city,
                countryCode = countryCode.uppercase()
            )
        }
        
        /**
         * Create a US address with state and zip code
         */
        fun us(streetAddress: String, city: String, state: String, zipCode: String): Address {
            return Address(
                streetAddress = streetAddress,
                city = city,
                stateProvince = state,
                postalCode = zipCode,
                countryCode = "US"
            )
        }
        
        /**
         * Create a Canadian address with province and postal code
         */
        fun canada(streetAddress: String, city: String, province: String, postalCode: String): Address {
            return Address(
                streetAddress = streetAddress,
                city = city,
                stateProvince = province,
                postalCode = postalCode,
                countryCode = "CA"
            )
        }
        
        /**
         * Common country codes for validation
         */
        val COMMON_COUNTRY_CODES = setOf(
            "US", "CA", "GB", "DE", "FR", "IT", "ES", "AU", "JP", "CN", "IN", "BR", "MX"
        )
    }
    
    init {
        require(countryCode.length == 2) { "Country code must be exactly 2 characters" }
        require(countryCode.all { it.isLetter() }) { "Country code must contain only letters" }
    }
    
    /**
     * Get the normalized country code (uppercase)
     */
    val normalizedCountryCode: String = countryCode.uppercase()
    
    /**
     * Check if this address is in the United States
     */
    fun isUnitedStates(): Boolean = normalizedCountryCode == "US"
    
    /**
     * Check if this address is in Canada
     */
    fun isCanada(): Boolean = normalizedCountryCode == "CA"
    
    /**
     * Check if this address requires a state/province
     */
    fun requiresStateProvince(): Boolean {
        return normalizedCountryCode in setOf("US", "CA", "AU", "BR", "IN")
    }
    
    /**
     * Get formatted single line address
     */
    fun toSingleLine(): String {
        val parts = mutableListOf<String>()
        
        parts.add(streetAddress)
        streetAddress2?.let { if (it.isNotBlank()) parts.add(it) }
        parts.add(city)
        stateProvince?.let { if (it.isNotBlank()) parts.add(it) }
        postalCode?.let { if (it.isNotBlank()) parts.add(it) }
        parts.add(normalizedCountryCode)
        
        return parts.joinToString(", ")
    }
    
    /**
     * Get formatted multi-line address
     */
    fun toMultiLine(): String {
        val lines = mutableListOf<String>()
        
        lines.add(streetAddress)
        streetAddress2?.let { if (it.isNotBlank()) lines.add(it) }
        
        val cityStateZip = mutableListOf<String>()
        cityStateZip.add(city)
        stateProvince?.let { if (it.isNotBlank()) cityStateZip.add(it) }
        postalCode?.let { if (it.isNotBlank()) cityStateZip.add(it) }
        lines.add(cityStateZip.joinToString(", "))
        
        lines.add(normalizedCountryCode)
        
        return lines.joinToString("\n")
    }
    
    /**
     * Validate address based on country-specific rules
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        // Basic validation
        if (streetAddress.isBlank()) {
            errors.add("Street address is required")
        }
        
        if (city.isBlank()) {
            errors.add("City is required")
        }
        
        if (normalizedCountryCode.isBlank()) {
            errors.add("Country code is required")
        }
        
        // Country-specific validation
        when (normalizedCountryCode) {
            "US" -> {
                if (stateProvince.isNullOrBlank()) {
                    errors.add("State is required for US addresses")
                }
                if (postalCode.isNullOrBlank()) {
                    errors.add("ZIP code is required for US addresses")
                } else if (!isValidUSZipCode(postalCode)) {
                    errors.add("Invalid US ZIP code format: $postalCode")
                }
            }
            "CA" -> {
                if (stateProvince.isNullOrBlank()) {
                    errors.add("Province is required for Canadian addresses")
                }
                if (postalCode.isNullOrBlank()) {
                    errors.add("Postal code is required for Canadian addresses")
                } else if (!isValidCanadianPostalCode(postalCode)) {
                    errors.add("Invalid Canadian postal code format: $postalCode")
                }
            }
        }
        
        return errors
    }
    
    private fun isValidUSZipCode(zipCode: String): Boolean {
        // US ZIP code: 12345 or 12345-6789
        val zipRegex = "^\\d{5}(-\\d{4})?$".toRegex()
        return zipRegex.matches(zipCode)
    }
    
    private fun isValidCanadianPostalCode(postalCode: String): Boolean {
        // Canadian postal code: A1A 1A1 or A1A1A1
        val canadianRegex = "^[A-Za-z]\\d[A-Za-z]\\s?\\d[A-Za-z]\\d$".toRegex()
        return canadianRegex.matches(postalCode)
    }
}
