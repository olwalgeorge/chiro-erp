package org.chiro.finance.domain.valueobject

import jakarta.persistence.Embeddable
import jakarta.persistence.Column
import jakarta.validation.constraints.NotNull
import java.util.*

/**
 * Journal Entry Identifier - Strongly Typed UUID for Transaction references
 * 
 * Provides type safety for financial transaction identifiers following
 * DDD principles and enterprise patterns.
 * 
 * @author Chiro ERP Finance Team
 * @since 1.0.0
 */
@Embeddable
@JvmInline
value class JournalEntryId(
    @field:NotNull
    @Column(name = "journal_entry_id", columnDefinition = "UUID")
    val value: UUID
) {
    companion object {
        fun generate(): JournalEntryId = JournalEntryId(UUID.randomUUID())
        fun fromString(value: String): JournalEntryId = JournalEntryId(UUID.fromString(value))
        fun from(uuid: UUID): JournalEntryId = JournalEntryId(uuid)
    }
    
    init {
        require(value.toString().isNotBlank()) { "JournalEntryId cannot be blank" }
    }
    
    override fun toString(): String = value.toString()
    fun toUUID(): UUID = value
}
