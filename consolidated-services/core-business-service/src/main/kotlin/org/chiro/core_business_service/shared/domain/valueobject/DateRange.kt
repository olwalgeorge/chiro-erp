package org.chiro.core_business_service.shared.domain.valueobject

import jakarta.persistence.Embeddable
import jakarta.persistence.Column
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Date Range Value Object - shared across all ERP modules.
 * 
 * Used for:
 * - Accounting periods
 * - Project durations  
 * - Employment periods
 * - Contract terms
 * - Reporting periods
 * - Promotional campaigns
 * 
 * Business Rules:
 * - Start date must be before or equal to end date
 * - Both dates are required
 * - Supports overlap checking with other date ranges
 * - Provides duration calculations
 */
@Embeddable
data class DateRange(
    
    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate,
    
    @Column(name = "end_date", nullable = false)
    val endDate: LocalDate

) : Comparable<DateRange> {
    
    init {
        require(!startDate.isAfter(endDate)) { 
            "Start date ($startDate) must be before or equal to end date ($endDate)" 
        }
    }
    
    companion object {
        
        /**
         * Create a date range for a specific year
         */
        fun forYear(year: Int): DateRange {
            return DateRange(
                startDate = LocalDate.of(year, 1, 1),
                endDate = LocalDate.of(year, 12, 31)
            )
        }
        
        /**
         * Create a date range for a specific month
         */
        fun forMonth(year: Int, month: Int): DateRange {
            val start = LocalDate.of(year, month, 1)
            val end = start.withDayOfMonth(start.lengthOfMonth())
            return DateRange(startDate = start, endDate = end)
        }
        
        /**
         * Create a date range for a specific quarter
         */
        fun forQuarter(year: Int, quarter: Int): DateRange {
            require(quarter in 1..4) { "Quarter must be between 1 and 4" }
            
            val startMonth = (quarter - 1) * 3 + 1
            val start = LocalDate.of(year, startMonth, 1)
            val end = start.plusMonths(2).withDayOfMonth(start.plusMonths(2).lengthOfMonth())
            
            return DateRange(startDate = start, endDate = end)
        }
        
        /**
         * Create a date range from today for a specific number of days
         */
        fun fromTodayForDays(days: Int): DateRange {
            val start = LocalDate.now()
            val end = start.plusDays(days.toLong() - 1)
            return DateRange(startDate = start, endDate = end)
        }
        
        /**
         * Create a single-day date range
         */
        fun singleDay(date: LocalDate): DateRange {
            return DateRange(startDate = date, endDate = date)
        }
    }
    
    /**
     * Get the duration in days (inclusive)
     */
    fun getDurationInDays(): Long {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
    }
    
    /**
     * Get the duration in weeks
     */
    fun getDurationInWeeks(): Long {
        return getDurationInDays() / 7
    }
    
    /**
     * Get the duration in months (approximate)
     */
    fun getDurationInMonths(): Long {
        return java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate)
    }
    
    /**
     * Check if this date range contains a specific date
     */
    fun contains(date: LocalDate): Boolean {
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }
    
    /**
     * Check if this date range overlaps with another date range
     */
    fun overlaps(other: DateRange): Boolean {
        return startDate <= other.endDate && endDate >= other.startDate
    }
    
    /**
     * Check if this date range is completely within another date range
     */
    fun isWithin(other: DateRange): Boolean {
        return !startDate.isBefore(other.startDate) && !endDate.isAfter(other.endDate)
    }
    
    /**
     * Check if this date range completely contains another date range
     */
    fun contains(other: DateRange): Boolean {
        return other.isWithin(this)
    }
    
    /**
     * Get the intersection of this date range with another
     */
    fun intersect(other: DateRange): DateRange? {
        if (!overlaps(other)) return null
        
        val intersectionStart = if (startDate.isAfter(other.startDate)) startDate else other.startDate
        val intersectionEnd = if (endDate.isBefore(other.endDate)) endDate else other.endDate
        
        return DateRange(intersectionStart, intersectionEnd)
    }
    
    /**
     * Extend this date range to include another date range
     */
    fun union(other: DateRange): DateRange {
        val unionStart = if (startDate.isBefore(other.startDate)) startDate else other.startDate
        val unionEnd = if (endDate.isAfter(other.endDate)) endDate else other.endDate
        
        return DateRange(unionStart, unionEnd)
    }
    
    /**
     * Check if this date range is in the past
     */
    fun isInPast(): Boolean {
        return endDate.isBefore(LocalDate.now())
    }
    
    /**
     * Check if this date range is in the future
     */
    fun isInFuture(): Boolean {
        return startDate.isAfter(LocalDate.now())
    }
    
    /**
     * Check if this date range includes today
     */
    fun includesNow(): Boolean {
        return contains(LocalDate.now())
    }
    
    /**
     * Check if this is a single day range
     */
    fun isSingleDay(): Boolean {
        return startDate == endDate
    }
    
    /**
     * Get formatted display string
     */
    fun toDisplayString(formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE): String {
        return if (isSingleDay()) {
            startDate.format(formatter)
        } else {
            "${startDate.format(formatter)} - ${endDate.format(formatter)}"
        }
    }
    
    /**
     * Get formatted display string for UI
     */
    fun toUIString(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        return if (isSingleDay()) {
            startDate.format(formatter)
        } else {
            "${startDate.format(formatter)} - ${endDate.format(formatter)}"
        }
    }
    
    override fun compareTo(other: DateRange): Int {
        val startComparison = startDate.compareTo(other.startDate)
        return if (startComparison != 0) startComparison else endDate.compareTo(other.endDate)
    }
}
