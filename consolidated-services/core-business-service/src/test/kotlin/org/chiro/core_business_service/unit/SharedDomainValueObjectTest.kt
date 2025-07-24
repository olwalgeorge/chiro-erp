package org.chiro.core_business_service.unit

import io.quarkus.test.junit.QuarkusTest
import org.chiro.core_business_service.shared.domain.valueobject.UUIDBasedAggregateId
import org.chiro.core_business_service.shared.domain.exception.ValueObjectValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import java.util.*

/**
 * Unit tests for shared domain value objects
 */
@QuarkusTest
class SharedDomainValueObjectTest {
    
    class TestAggregateId(value: String = UUID.randomUUID().toString()) : UUIDBasedAggregateId(value)
    
    @Test
    fun `should create valid aggregate id with UUID`() {
        // Given
        val uuid = UUID.randomUUID().toString()
        
        // When
        val aggregateId = TestAggregateId(uuid)
        
        // Then
        assertEquals(uuid, aggregateId.value)
    }
    
    @Test
    fun `should generate random UUID when no value provided`() {
        // When
        val aggregateId1 = TestAggregateId()
        val aggregateId2 = TestAggregateId()
        
        // Then
        assertNotEquals(aggregateId1.value, aggregateId2.value)
    }
    
    @Test
    fun `should throw exception for invalid UUID`() {
        // Given
        val invalidUuid = "not-a-uuid"
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            TestAggregateId(invalidUuid)
        }
    }
    
    @Test
    fun `should throw exception for blank value`() {
        // Given
        val blankValue = ""
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            TestAggregateId(blankValue)
        }
    }
    
    @Test
    fun `should have equality based on value`() {
        // Given
        val uuid = UUID.randomUUID().toString()
        val aggregateId1 = TestAggregateId(uuid)
        val aggregateId2 = TestAggregateId(uuid)
        
        // Then
        assertEquals(aggregateId1, aggregateId2)
        assertEquals(aggregateId1.hashCode(), aggregateId2.hashCode())
    }
    
    @Test
    fun `should have string representation of value`() {
        // Given
        val uuid = UUID.randomUUID().toString()
        val aggregateId = TestAggregateId(uuid)
        
        // Then
        assertEquals(uuid, aggregateId.toString())
    }
}
