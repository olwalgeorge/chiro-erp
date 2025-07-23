package org.chiro.finance.domain.valueobject

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.io.Serializable

/**
 * Payment Method Value Object
 * 
 * Represents the different methods of payment available in the ERP system.
 * This value object encapsulates payment method types with validation and business rules.
 * 
 * Design Pattern: Value Object (immutable, equality based on value)
 * Domain: Finance
 * 
 * @author Chiro ERP Team
 * @version 1.0
 * @since 2025-01-24
 */
enum class PaymentMethodType {
    // ==================== CASH PAYMENTS ====================
    CASH,
    PETTY_CASH,
    CASH_ON_DELIVERY,
    
    // ==================== BANK TRANSFERS ====================
    WIRE_TRANSFER,
    ACH_TRANSFER,
    BANK_DRAFT,
    ELECTRONIC_FUNDS_TRANSFER,
    SWIFT_TRANSFER,
    
    // ==================== CHECKS ====================
    PERSONAL_CHECK,
    CASHIERS_CHECK,
    CERTIFIED_CHECK,
    MONEY_ORDER,
    TRAVELERS_CHECK,
    
    // ==================== CREDIT/DEBIT CARDS ====================
    CREDIT_CARD,
    DEBIT_CARD,
    PREPAID_CARD,
    CORPORATE_CARD,
    
    // ==================== DIGITAL PAYMENTS ====================
    PAYPAL,
    STRIPE,
    SQUARE,
    APPLE_PAY,
    GOOGLE_PAY,
    SAMSUNG_PAY,
    VENMO,
    ZELLE,
    
    // ==================== CRYPTOCURRENCY ====================
    BITCOIN,
    ETHEREUM,
    CRYPTOCURRENCY_OTHER,
    
    // ==================== MOBILE PAYMENTS ====================
    MOBILE_WALLET,
    QR_CODE_PAYMENT,
    NFC_PAYMENT,
    
    // ==================== TRADE/BARTER ====================
    TRADE_CREDIT,
    BARTER_EXCHANGE,
    STORE_CREDIT,
    GIFT_CARD,
    LOYALTY_POINTS,
    
    // ==================== FINANCING ====================
    INSTALLMENT_PAYMENT,
    LEASE_PAYMENT,
    FINANCING_TERMS,
    LAYAWAY,
    
    // ==================== INTERNATIONAL ====================
    INTERNATIONAL_WIRE,
    FOREIGN_EXCHANGE,
    LETTER_OF_CREDIT,
    
    // ==================== SPECIAL/OTHER ====================
    ESCROW,
    COLLATERAL,
    OFFSET,
    CONTRA_ACCOUNT,
    SYSTEM_GENERATED,
    OTHER
}

/**
 * Payment Method Value Object
 * 
 * Encapsulates payment method information with validation and business logic.
 * This is an immutable value object that represents how a payment is made.
 */
data class PaymentMethod(
    @field:NotBlank(message = "Payment method type cannot be blank")
    val type: PaymentMethodType,
    
    @field:Size(max = 100, message = "Payment method name cannot exceed 100 characters")
    val name: String = type.getDisplayName(),
    
    @field:Size(max = 500, message = "Payment method description cannot exceed 500 characters")
    val description: String? = null,
    
    val isActive: Boolean = true,
    val requiresManualApproval: Boolean = false,
    val supportsRefunds: Boolean = true,
    val supportsPartialPayments: Boolean = true,
    val isRealTime: Boolean = false,
    val maxTransactionAmount: java.math.BigDecimal? = null,
    val minTransactionAmount: java.math.BigDecimal? = null,
    val processingFeePercentage: java.math.BigDecimal? = null,
    val processingFeeFixed: java.math.BigDecimal? = null,
    
    // Audit fields
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val lastModifiedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
) : Serializable {
    
    companion object {
        private const val serialVersionUID = 1L
        
        // ==================== FACTORY METHODS ====================
        
        /**
         * Creates a cash payment method
         */
        fun cash(): PaymentMethod = PaymentMethod(
            type = PaymentMethodType.CASH,
            isRealTime = true,
            supportsRefunds = false
        )
        
        /**
         * Creates a credit card payment method
         */
        fun creditCard(
            processingFeePercentage: java.math.BigDecimal = java.math.BigDecimal("2.9")
        ): PaymentMethod = PaymentMethod(
            type = PaymentMethodType.CREDIT_CARD,
            isRealTime = true,
            processingFeePercentage = processingFeePercentage,
            requiresManualApproval = false
        )
        
        /**
         * Creates a bank transfer payment method
         */
        fun bankTransfer(): PaymentMethod = PaymentMethod(
            type = PaymentMethodType.WIRE_TRANSFER,
            isRealTime = false,
            requiresManualApproval = true
        )
        
        /**
         * Creates a check payment method
         */
        fun check(): PaymentMethod = PaymentMethod(
            type = PaymentMethodType.PERSONAL_CHECK,
            isRealTime = false,
            requiresManualApproval = true,
            supportsPartialPayments = false
        )
        
        /**
         * Creates a digital payment method
         */
        fun digitalPayment(type: PaymentMethodType): PaymentMethod {
            require(type.isDigitalPayment()) { "Type must be a digital payment method" }
            return PaymentMethod(
                type = type,
                isRealTime = true,
                processingFeePercentage = java.math.BigDecimal("2.9")
            )
        }
        
        /**
         * Creates a cryptocurrency payment method
         */
        fun cryptocurrency(type: PaymentMethodType): PaymentMethod {
            require(type.isCryptocurrency()) { "Type must be a cryptocurrency payment method" }
            return PaymentMethod(
                type = type,
                isRealTime = false,
                requiresManualApproval = true,
                supportsRefunds = false
            )
        }
    }
    
    // ==================== BUSINESS LOGIC ====================
    
    /**
     * Validates if this payment method can process the given amount
     */
    fun canProcessAmount(amount: java.math.BigDecimal): Boolean {
        if (amount <= java.math.BigDecimal.ZERO) return false
        
        minTransactionAmount?.let { min ->
            if (amount < min) return false
        }
        
        maxTransactionAmount?.let { max ->
            if (amount > max) return false
        }
        
        return true
    }
    
    /**
     * Calculates the processing fee for a given amount
     */
    fun calculateProcessingFee(amount: java.math.BigDecimal): java.math.BigDecimal {
        var fee = java.math.BigDecimal.ZERO
        
        processingFeePercentage?.let { percentage ->
            fee = fee.add(amount.multiply(percentage.divide(java.math.BigDecimal("100"))))
        }
        
        processingFeeFixed?.let { fixed ->
            fee = fee.add(fixed)
        }
        
        return fee
    }
    
    /**
     * Calculates the net amount after processing fees
     */
    fun calculateNetAmount(grossAmount: java.math.BigDecimal): java.math.BigDecimal {
        return grossAmount.subtract(calculateProcessingFee(grossAmount))
    }
    
    /**
     * Checks if this payment method requires additional verification
     */
    fun requiresVerification(): Boolean {
        return requiresManualApproval || type.isHighRisk()
    }
    
    /**
     * Checks if this payment method supports instant settlement
     */
    fun supportsInstantSettlement(): Boolean {
        return isRealTime && !requiresManualApproval
    }
    
    /**
     * Gets the expected settlement time in business days
     */
    fun getSettlementDays(): Int {
        return when {
            isRealTime && !requiresManualApproval -> 0
            type.isBankTransfer() -> 1
            type.isCheck() -> 3
            type.isCryptocurrency() -> 1
            else -> 1
        }
    }
    
    // ==================== VALIDATION ====================
    
    init {
        require(name.isNotBlank()) { "Payment method name cannot be blank" }
        require(name.length <= 100) { "Payment method name cannot exceed 100 characters" }
        description?.let { desc ->
            require(desc.length <= 500) { "Payment method description cannot exceed 500 characters" }
        }
        
        maxTransactionAmount?.let { max ->
            require(max > java.math.BigDecimal.ZERO) { "Max transaction amount must be positive" }
        }
        
        minTransactionAmount?.let { min ->
            require(min >= java.math.BigDecimal.ZERO) { "Min transaction amount must be non-negative" }
        }
        
        if (maxTransactionAmount != null && minTransactionAmount != null) {
            require(maxTransactionAmount >= minTransactionAmount) { 
                "Max transaction amount must be greater than or equal to min transaction amount" 
            }
        }
        
        processingFeePercentage?.let { percentage ->
            require(percentage >= java.math.BigDecimal.ZERO && percentage <= java.math.BigDecimal("100")) {
                "Processing fee percentage must be between 0 and 100"
            }
        }
        
        processingFeeFixed?.let { fixed ->
            require(fixed >= java.math.BigDecimal.ZERO) { "Processing fee fixed amount must be non-negative" }
        }
    }
}

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Extension functions for PaymentMethodType enum
 */
fun PaymentMethodType.getDisplayName(): String = when (this) {
    PaymentMethodType.CASH -> "Cash"
    PaymentMethodType.PETTY_CASH -> "Petty Cash"
    PaymentMethodType.CASH_ON_DELIVERY -> "Cash on Delivery"
    PaymentMethodType.WIRE_TRANSFER -> "Wire Transfer"
    PaymentMethodType.ACH_TRANSFER -> "ACH Transfer"
    PaymentMethodType.BANK_DRAFT -> "Bank Draft"
    PaymentMethodType.ELECTRONIC_FUNDS_TRANSFER -> "Electronic Funds Transfer"
    PaymentMethodType.SWIFT_TRANSFER -> "SWIFT Transfer"
    PaymentMethodType.PERSONAL_CHECK -> "Personal Check"
    PaymentMethodType.CASHIERS_CHECK -> "Cashier's Check"
    PaymentMethodType.CERTIFIED_CHECK -> "Certified Check"
    PaymentMethodType.MONEY_ORDER -> "Money Order"
    PaymentMethodType.TRAVELERS_CHECK -> "Traveler's Check"
    PaymentMethodType.CREDIT_CARD -> "Credit Card"
    PaymentMethodType.DEBIT_CARD -> "Debit Card"
    PaymentMethodType.PREPAID_CARD -> "Prepaid Card"
    PaymentMethodType.CORPORATE_CARD -> "Corporate Card"
    PaymentMethodType.PAYPAL -> "PayPal"
    PaymentMethodType.STRIPE -> "Stripe"
    PaymentMethodType.SQUARE -> "Square"
    PaymentMethodType.APPLE_PAY -> "Apple Pay"
    PaymentMethodType.GOOGLE_PAY -> "Google Pay"
    PaymentMethodType.SAMSUNG_PAY -> "Samsung Pay"
    PaymentMethodType.VENMO -> "Venmo"
    PaymentMethodType.ZELLE -> "Zelle"
    PaymentMethodType.BITCOIN -> "Bitcoin"
    PaymentMethodType.ETHEREUM -> "Ethereum"
    PaymentMethodType.CRYPTOCURRENCY_OTHER -> "Other Cryptocurrency"
    PaymentMethodType.MOBILE_WALLET -> "Mobile Wallet"
    PaymentMethodType.QR_CODE_PAYMENT -> "QR Code Payment"
    PaymentMethodType.NFC_PAYMENT -> "NFC Payment"
    PaymentMethodType.TRADE_CREDIT -> "Trade Credit"
    PaymentMethodType.BARTER_EXCHANGE -> "Barter Exchange"
    PaymentMethodType.STORE_CREDIT -> "Store Credit"
    PaymentMethodType.GIFT_CARD -> "Gift Card"
    PaymentMethodType.LOYALTY_POINTS -> "Loyalty Points"
    PaymentMethodType.INSTALLMENT_PAYMENT -> "Installment Payment"
    PaymentMethodType.LEASE_PAYMENT -> "Lease Payment"
    PaymentMethodType.FINANCING_TERMS -> "Financing Terms"
    PaymentMethodType.LAYAWAY -> "Layaway"
    PaymentMethodType.INTERNATIONAL_WIRE -> "International Wire"
    PaymentMethodType.FOREIGN_EXCHANGE -> "Foreign Exchange"
    PaymentMethodType.LETTER_OF_CREDIT -> "Letter of Credit"
    PaymentMethodType.ESCROW -> "Escrow"
    PaymentMethodType.COLLATERAL -> "Collateral"
    PaymentMethodType.OFFSET -> "Offset"
    PaymentMethodType.CONTRA_ACCOUNT -> "Contra Account"
    PaymentMethodType.SYSTEM_GENERATED -> "System Generated"
    PaymentMethodType.OTHER -> "Other"
}

fun PaymentMethodType.isDigitalPayment(): Boolean = when (this) {
    PaymentMethodType.PAYPAL,
    PaymentMethodType.STRIPE,
    PaymentMethodType.SQUARE,
    PaymentMethodType.APPLE_PAY,
    PaymentMethodType.GOOGLE_PAY,
    PaymentMethodType.SAMSUNG_PAY,
    PaymentMethodType.VENMO,
    PaymentMethodType.ZELLE,
    PaymentMethodType.MOBILE_WALLET,
    PaymentMethodType.QR_CODE_PAYMENT,
    PaymentMethodType.NFC_PAYMENT -> true
    else -> false
}

fun PaymentMethodType.isCryptocurrency(): Boolean = when (this) {
    PaymentMethodType.BITCOIN,
    PaymentMethodType.ETHEREUM,
    PaymentMethodType.CRYPTOCURRENCY_OTHER -> true
    else -> false
}

fun PaymentMethodType.isBankTransfer(): Boolean = when (this) {
    PaymentMethodType.WIRE_TRANSFER,
    PaymentMethodType.ACH_TRANSFER,
    PaymentMethodType.BANK_DRAFT,
    PaymentMethodType.ELECTRONIC_FUNDS_TRANSFER,
    PaymentMethodType.SWIFT_TRANSFER,
    PaymentMethodType.INTERNATIONAL_WIRE -> true
    else -> false
}

fun PaymentMethodType.isCheck(): Boolean = when (this) {
    PaymentMethodType.PERSONAL_CHECK,
    PaymentMethodType.CASHIERS_CHECK,
    PaymentMethodType.CERTIFIED_CHECK,
    PaymentMethodType.MONEY_ORDER,
    PaymentMethodType.TRAVELERS_CHECK -> true
    else -> false
}

fun PaymentMethodType.isCard(): Boolean = when (this) {
    PaymentMethodType.CREDIT_CARD,
    PaymentMethodType.DEBIT_CARD,
    PaymentMethodType.PREPAID_CARD,
    PaymentMethodType.CORPORATE_CARD -> true
    else -> false
}

fun PaymentMethodType.isHighRisk(): Boolean = when (this) {
    PaymentMethodType.BITCOIN,
    PaymentMethodType.ETHEREUM,
    PaymentMethodType.CRYPTOCURRENCY_OTHER,
    PaymentMethodType.INTERNATIONAL_WIRE,
    PaymentMethodType.FOREIGN_EXCHANGE,
    PaymentMethodType.CASH -> true
    else -> false
}

fun PaymentMethodType.getCategory(): String = when (this) {
    PaymentMethodType.CASH, PaymentMethodType.PETTY_CASH, PaymentMethodType.CASH_ON_DELIVERY -> "Cash"
    PaymentMethodType.WIRE_TRANSFER, PaymentMethodType.ACH_TRANSFER, PaymentMethodType.BANK_DRAFT,
    PaymentMethodType.ELECTRONIC_FUNDS_TRANSFER, PaymentMethodType.SWIFT_TRANSFER, 
    PaymentMethodType.INTERNATIONAL_WIRE -> "Bank Transfer"
    PaymentMethodType.PERSONAL_CHECK, PaymentMethodType.CASHIERS_CHECK, PaymentMethodType.CERTIFIED_CHECK,
    PaymentMethodType.MONEY_ORDER, PaymentMethodType.TRAVELERS_CHECK -> "Check"
    PaymentMethodType.CREDIT_CARD, PaymentMethodType.DEBIT_CARD, PaymentMethodType.PREPAID_CARD,
    PaymentMethodType.CORPORATE_CARD -> "Card"
    PaymentMethodType.PAYPAL, PaymentMethodType.STRIPE, PaymentMethodType.SQUARE,
    PaymentMethodType.APPLE_PAY, PaymentMethodType.GOOGLE_PAY, PaymentMethodType.SAMSUNG_PAY,
    PaymentMethodType.VENMO, PaymentMethodType.ZELLE -> "Digital Payment"
    PaymentMethodType.BITCOIN, PaymentMethodType.ETHEREUM, PaymentMethodType.CRYPTOCURRENCY_OTHER -> "Cryptocurrency"
    PaymentMethodType.MOBILE_WALLET, PaymentMethodType.QR_CODE_PAYMENT, PaymentMethodType.NFC_PAYMENT -> "Mobile Payment"
    PaymentMethodType.TRADE_CREDIT, PaymentMethodType.BARTER_EXCHANGE, PaymentMethodType.STORE_CREDIT,
    PaymentMethodType.GIFT_CARD, PaymentMethodType.LOYALTY_POINTS -> "Trade/Credit"
    PaymentMethodType.INSTALLMENT_PAYMENT, PaymentMethodType.LEASE_PAYMENT, PaymentMethodType.FINANCING_TERMS,
    PaymentMethodType.LAYAWAY -> "Financing"
    PaymentMethodType.FOREIGN_EXCHANGE, PaymentMethodType.LETTER_OF_CREDIT -> "International"
    PaymentMethodType.ESCROW, PaymentMethodType.COLLATERAL, PaymentMethodType.OFFSET,
    PaymentMethodType.CONTRA_ACCOUNT, PaymentMethodType.SYSTEM_GENERATED, PaymentMethodType.OTHER -> "Special"
}
