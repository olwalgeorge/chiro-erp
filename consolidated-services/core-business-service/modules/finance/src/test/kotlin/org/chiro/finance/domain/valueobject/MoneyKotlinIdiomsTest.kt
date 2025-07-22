package org.chiro.finance.domain.valueobject

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

/**
 * Test demonstrating world-class Kotlin idioms in Money value object
 */
class MoneyKotlinIdiomsTest {
    
    @Test
    fun `should demonstrate idiomatic Kotlin DSL creation`() {
        // Kotlin DSL-style Money creation
        val price = "100.50".USD
        val discount = "10.25".USD
        val tax = "15.75".USD
        
        assertEquals(Money.of("100.50", "USD"), price)
        assertEquals(Money.of("10.25", "USD"), discount)
        assertEquals(Money.of("15.75", "USD"), tax)
    }
    
    @Test
    fun `should demonstrate operator overloading`() {
        val basePrice = "100.00".USD
        val discount = "10.00".USD
        val taxRate = BigDecimal("0.15") // 15% tax
        
        // Using Kotlin operators instead of verbose method calls
        val discountedPrice = basePrice - discount  // operator fun minus()
        val finalPrice = discountedPrice + (discountedPrice * taxRate)  // operator fun plus() and times()
        
        assertEquals("90.00".USD, discountedPrice)
        assertEquals("103.50".USD, finalPrice)
    }
    
    @Test
    fun `should demonstrate property-style boolean checks`() {
        val positive = "100.00".USD
        val negative = "-50.00".USD
        val zero = Money.zero("USD")
        
        // Using Kotlin properties instead of methods
        assertTrue(positive.isPositive)
        assertFalse(positive.isNegative)
        assertFalse(positive.isZero)
        
        assertTrue(negative.isNegative)
        assertFalse(negative.isPositive)
        
        assertTrue(zero.isZero)
        assertFalse(zero.isPositive)
        assertFalse(zero.isNegative)
    }
    
    @Test
    fun `should demonstrate unary operators`() {
        val amount = "100.00".USD
        
        val positive = +amount  // unary plus
        val negative = -amount  // unary minus
        
        assertEquals(amount, positive)
        assertEquals("-100.00".USD, negative)
    }
    
    @Test
    fun `should demonstrate infix notation`() {
        // Using infix notation for currency creation
        val priceInEur = "85.50" of "EUR"
        val priceInJpy = 12500 of "JPY"
        val priceInGbp = 75.25 of "GBP"
        
        assertEquals(Money.of("85.50", "EUR"), priceInEur)
        assertEquals(Money.of(12500.0, "JPY"), priceInJpy)
        assertEquals(Money.of(75.25, "GBP"), priceInGbp)
    }
    
    @Test
    fun `should demonstrate collection operations`() {
        val prices = listOf(
            "100.00".USD,
            "50.25".USD,
            "75.75".USD,
            "25.00".USD
        )
        
        // Using Kotlin extension function for sum
        val total = prices.sum()
        val totalOrZero = prices.sumOrZero()
        
        assertEquals("251.00".USD, total)
        assertEquals("251.00".USD, totalOrZero)
        
        // Test empty collection
        val emptyPrices = emptyList<Money>()
        assertNull(emptyPrices.sum())
        assertEquals(Money.zero("USD"), emptyPrices.sumOrZero("USD"))
    }
    
    @Test
    fun `should demonstrate different currency extensions`() {
        val usdAmount = 100.USD
        val eurAmount = 85.50.EUR
        val gbpAmount = "75.25".GBP
        val jpyAmount = "12500".JPY
        
        assertEquals("USD", usdAmount.currencyCode)
        assertEquals("EUR", eurAmount.currencyCode)
        assertEquals("GBP", gbpAmount.currencyCode)
        assertEquals("JPY", jpyAmount.currencyCode)
    }
    
    @Test
    fun `should demonstrate mathematical operations with different types`() {
        val baseAmount = "100.00".USD
        
        // Multiply with different numeric types
        val doubledInt = baseAmount * 2           // Int
        val tripledDouble = baseAmount * 3.0      // Double
        val factorBigDecimal = baseAmount * BigDecimal("2.5")  // BigDecimal
        
        assertEquals("200.00".USD, doubledInt)
        assertEquals("300.00".USD, tripledDouble)
        assertEquals("250.00".USD, factorBigDecimal)
        
        // Division with different numeric types
        val halfInt = baseAmount / 2              // Int
        val thirdDouble = baseAmount / 3.0        // Double
        val quarterBigDecimal = baseAmount / BigDecimal("4")   // BigDecimal
        
        assertEquals("50.00".USD, halfInt)
        assertEquals("33.3333".USD, thirdDouble.round())  // Rounded to currency precision
        assertEquals("25.00".USD, quarterBigDecimal)
    }
    
    @Test
    fun `should demonstrate advanced financial operations`() {
        val principal = "1000.00".USD
        val rate = BigDecimal("0.05")  // 5% interest
        val periods = 3
        
        // Compound interest calculation
        val futureValue = principal.calculateCompoundInterest(rate, periods)
        assertEquals("1157.6250".USD, futureValue.round())
        
        // Discount and tax operations
        val originalPrice = "100.00".USD
        val discountedPrice = originalPrice.applyDiscount(BigDecimal("10"))  // 10% discount
        val finalPrice = discountedPrice.applyTax(BigDecimal("8.5"))  // 8.5% tax
        
        assertEquals("90.00".USD, discountedPrice)
        assertEquals("97.6500".USD, finalPrice.round())
    }
    
    @Test
    fun `should demonstrate currency conversion`() {
        val usdAmount = "100.00".USD
        val exchangeRate = BigDecimal("0.85")  // 1 USD = 0.85 EUR
        
        val eurAmount = usdAmount.convertTo("EUR", exchangeRate)
        
        assertEquals("85.00".EUR, eurAmount)
        assertEquals("EUR", eurAmount.currencyCode)
    }
    
    @Test
    fun `should demonstrate smart distribution`() {
        val totalAmount = "100.00".USD
        val parts = 3
        
        // Smart distribution that handles remainders fairly
        val distribution = totalAmount.distribute(parts)
        
        assertEquals(3, distribution.size)
        
        // The sum should equal the original amount
        val redistributed = distribution.sum()
        assertEquals(totalAmount, redistributed)
        
        // Check that differences are minimal
        val maxDifference = distribution.maxOf { it.amount } - distribution.minOf { it.amount }
        assertTrue(maxDifference <= BigDecimal("0.01"))
    }
}
