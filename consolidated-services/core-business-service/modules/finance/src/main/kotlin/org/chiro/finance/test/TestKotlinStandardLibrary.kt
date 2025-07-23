package org.chiro.finance.test

import org.chiro.finance.domain.valueobject.Money
import java.util.UUID

/**
 * Simple test class to verify Kotlin standard library and basic functionality
 */
class TestKotlinStandardLibrary {
    
    fun testBasicKotlinFunctions(): Boolean {
        // Test require function
        require(true) { "This should not fail" }
        
        // Test apply function
        val testObject = TestData().apply {
            name = "Test"
            value = 42
        }
        
        // Test let function
        val result = testObject.let { data ->
            data.name.length + data.value
        }
        
        return result > 0
    }
    
    fun testUUID(): UUID {
        return UUID.randomUUID()
    }
    
    fun testMoney(): Money {
        return Money.zero("USD")
    }
}

data class TestData(
    var name: String = "",
    var value: Int = 0
)
