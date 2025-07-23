package org.chiro.finance.domain.valueobject

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Max
import java.io.Serializable
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Accounting Period Value Object
 * 
 * Represents financial reporting periods for accounting and business operations.
 * This value object encapsulates period definitions, fiscal year handling, and period calculations.
 * 
 * Design Pattern: Value Object (immutable, equality based on value)
 * Domain: Finance
 * 
 * @author Chiro ERP Team
 * @version 1.0
 * @since 2025-01-24
 */
enum class PeriodType {
    // ==================== STANDARD PERIODS ====================
    DAILY,                      // Daily periods
    WEEKLY,                     // Weekly periods (7 days)
    MONTHLY,                    // Monthly periods (calendar months)
    QUARTERLY,                  // Quarterly periods (3 months)
    SEMI_ANNUAL,               // Semi-annual periods (6 months)
    ANNUAL,                    // Annual periods (12 months)
    
    // ==================== FISCAL PERIODS ====================
    FISCAL_MONTHLY,            // Fiscal monthly periods
    FISCAL_QUARTERLY,          // Fiscal quarterly periods
    FISCAL_SEMI_ANNUAL,        // Fiscal semi-annual periods
    FISCAL_ANNUAL,             // Fiscal annual periods
    
    // ==================== SPECIAL PERIODS ====================
    CUSTOM,                    // Custom defined periods
    ROLLING_PERIOD,            // Rolling periods (e.g., last 30 days)
    PROJECT_PERIOD,            // Project-based periods
    SEASONAL_PERIOD,           // Seasonal periods
    
    // ==================== REPORTING PERIODS ====================
    YEAR_TO_DATE,              // Year-to-date period
    QUARTER_TO_DATE,           // Quarter-to-date period
    MONTH_TO_DATE,             // Month-to-date period
    WEEK_TO_DATE,              // Week-to-date period
    
    // ==================== COMPARATIVE PERIODS ====================
    PRIOR_YEAR,                // Prior year same period
    PRIOR_QUARTER,             // Prior quarter same period
    PRIOR_MONTH,               // Prior month same period
    SAME_PERIOD_LAST_YEAR      // Same period last year
}

/**
 * Accounting Period Value Object
 * 
 * Encapsulates a specific time period for financial reporting and analysis.
 * This is an immutable value object that represents a defined accounting period.
 */
data class AccountingPeriod(
    @field:NotNull(message = "Period type cannot be null")
    val type: PeriodType,
    
    @field:NotNull(message = "Start date cannot be null")
    val startDate: LocalDate,
    
    @field:NotNull(message = "End date cannot be null")
    val endDate: LocalDate,
    
    val name: String = generatePeriodName(type, startDate, endDate),
    val description: String? = null,
    
    // ==================== FISCAL YEAR SETTINGS ====================
    val fiscalYearStartMonth: Month = Month.JANUARY,
    val fiscalYearEndMonth: Month = Month.DECEMBER,
    
    // ==================== PERIOD METADATA ====================
    @field:Min(value = 1, message = "Period number must be at least 1")
    @field:Max(value = 366, message = "Period number cannot exceed 366")
    val periodNumber: Int = calculatePeriodNumber(type, startDate, fiscalYearStartMonth),
    
    @field:Min(value = 1, message = "Fiscal year must be at least 1")
    val fiscalYear: Int = calculateFiscalYear(startDate, fiscalYearStartMonth),
    
    val isActive: Boolean = true,
    val isClosed: Boolean = false,
    val isAdjustmentPeriod: Boolean = false,
    
    // ==================== AUDIT FIELDS ====================
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val lastModifiedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
) : Serializable, Comparable<AccountingPeriod> {
    
    companion object {
        private const val serialVersionUID = 1L
        
        // ==================== FACTORY METHODS ====================
        
        /**
         * Creates a monthly accounting period
         */
        fun monthly(year: Int, month: Month): AccountingPeriod {
            val yearMonth = YearMonth.of(year, month)
            return AccountingPeriod(
                type = PeriodType.MONTHLY,
                startDate = yearMonth.atDay(1),
                endDate = yearMonth.atEndOfMonth()
            )
        }
        
        /**
         * Creates a quarterly accounting period
         */
        fun quarterly(year: Int, quarter: Int): AccountingPeriod {
            require(quarter in 1..4) { "Quarter must be between 1 and 4" }
            
            val startMonth = when (quarter) {
                1 -> Month.JANUARY
                2 -> Month.APRIL
                3 -> Month.JULY
                4 -> Month.OCTOBER
                else -> throw IllegalArgumentException("Invalid quarter: $quarter")
            }
            
            val startDate = LocalDate.of(year, startMonth, 1)
            val endDate = startDate.plusMonths(3).minusDays(1)
            
            return AccountingPeriod(
                type = PeriodType.QUARTERLY,
                startDate = startDate,
                endDate = endDate
            )
        }
        
        /**
         * Creates an annual accounting period
         */
        fun annual(year: Int): AccountingPeriod {
            return AccountingPeriod(
                type = PeriodType.ANNUAL,
                startDate = LocalDate.of(year, Month.JANUARY, 1),
                endDate = LocalDate.of(year, Month.DECEMBER, 31)
            )
        }
        
        /**
         * Creates a fiscal year period
         */
        fun fiscalYear(
            fiscalYear: Int,
            fiscalYearStartMonth: Month = Month.JULY
        ): AccountingPeriod {
            val startDate = LocalDate.of(fiscalYear, fiscalYearStartMonth, 1)
            val endDate = startDate.plusYears(1).minusDays(1)
            
            return AccountingPeriod(
                type = PeriodType.FISCAL_ANNUAL,
                startDate = startDate,
                endDate = endDate,
                fiscalYearStartMonth = fiscalYearStartMonth,
                fiscalYearEndMonth = fiscalYearStartMonth.minus(1),
                fiscalYear = fiscalYear
            )
        }
        
        /**
         * Creates a custom period
         */
        fun custom(
            name: String,
            startDate: LocalDate,
            endDate: LocalDate,
            description: String? = null
        ): AccountingPeriod {
            return AccountingPeriod(
                type = PeriodType.CUSTOM,
                startDate = startDate,
                endDate = endDate,
                name = name,
                description = description
            )
        }
        
        /**
         * Creates a year-to-date period
         */
        fun yearToDate(asOfDate: LocalDate = LocalDate.now()): AccountingPeriod {
            val startDate = LocalDate.of(asOfDate.year, Month.JANUARY, 1)
            return AccountingPeriod(
                type = PeriodType.YEAR_TO_DATE,
                startDate = startDate,
                endDate = asOfDate
            )
        }
        
        /**
         * Creates a month-to-date period
         */
        fun monthToDate(asOfDate: LocalDate = LocalDate.now()): AccountingPeriod {
            val startDate = asOfDate.withDayOfMonth(1)
            return AccountingPeriod(
                type = PeriodType.MONTH_TO_DATE,
                startDate = startDate,
                endDate = asOfDate
            )
        }
        
        /**
         * Creates a rolling period (e.g., last 30 days)
         */
        fun rollingDays(days: Int, endDate: LocalDate = LocalDate.now()): AccountingPeriod {
            val startDate = endDate.minusDays(days.toLong() - 1)
            return AccountingPeriod(
                type = PeriodType.ROLLING_PERIOD,
                startDate = startDate,
                endDate = endDate,
                name = "Last $days Days"
            )
        }
        
        /**
         * Creates the current period based on today's date
         */
        fun current(type: PeriodType): AccountingPeriod {
            val today = LocalDate.now()
            return when (type) {
                PeriodType.MONTHLY -> monthly(today.year, today.month)
                PeriodType.QUARTERLY -> {
                    val quarter = ((today.monthValue - 1) / 3) + 1
                    quarterly(today.year, quarter)
                }
                PeriodType.ANNUAL -> annual(today.year)
                PeriodType.YEAR_TO_DATE -> yearToDate(today)
                PeriodType.MONTH_TO_DATE -> monthToDate(today)
                else -> throw IllegalArgumentException("Cannot create current period for type: $type")
            }
        }
        
        // ==================== HELPER METHODS ====================
        
        private fun generatePeriodName(type: PeriodType, startDate: LocalDate, endDate: LocalDate): String {
            val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
            
            return when (type) {
                PeriodType.MONTHLY -> startDate.format(formatter)
                PeriodType.QUARTERLY -> {
                    val quarter = ((startDate.monthValue - 1) / 3) + 1
                    "Q$quarter ${startDate.year}"
                }
                PeriodType.ANNUAL -> "FY ${startDate.year}"
                PeriodType.FISCAL_ANNUAL -> "FY ${startDate.year}"
                PeriodType.YEAR_TO_DATE -> "YTD ${endDate.format(formatter)}"
                PeriodType.MONTH_TO_DATE -> "MTD ${endDate.format(formatter)}"
                PeriodType.ROLLING_PERIOD -> {
                    val days = ChronoUnit.DAYS.between(startDate, endDate) + 1
                    "Last $days Days"
                }
                else -> "${startDate.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}"
            }
        }
        
        private fun calculatePeriodNumber(type: PeriodType, startDate: LocalDate, fiscalYearStartMonth: Month): Int {
            return when (type) {
                PeriodType.MONTHLY, PeriodType.FISCAL_MONTHLY -> {
                    val fiscalMonthNumber = if (startDate.month.value >= fiscalYearStartMonth.value) {
                        startDate.month.value - fiscalYearStartMonth.value + 1
                    } else {
                        startDate.month.value + 12 - fiscalYearStartMonth.value + 1
                    }
                    fiscalMonthNumber
                }
                PeriodType.QUARTERLY, PeriodType.FISCAL_QUARTERLY -> {
                    val monthNumber = calculatePeriodNumber(PeriodType.MONTHLY, startDate, fiscalYearStartMonth)
                    ((monthNumber - 1) / 3) + 1
                }
                PeriodType.DAILY -> startDate.dayOfYear
                PeriodType.WEEKLY -> {
                    val weekOfYear = startDate.format(DateTimeFormatter.ofPattern("w")).toInt()
                    weekOfYear
                }
                else -> 1
            }
        }
        
        private fun calculateFiscalYear(startDate: LocalDate, fiscalYearStartMonth: Month): Int {
            return if (startDate.month.value >= fiscalYearStartMonth.value) {
                startDate.year
            } else {
                startDate.year - 1
            }
        }
    }
    
    // ==================== COMPUTED PROPERTIES ====================
    
    /**
     * Number of days in this period
     */
    val daysInPeriod: Long
        get() = ChronoUnit.DAYS.between(startDate, endDate) + 1
    
    /**
     * Check if this period contains the given date
     */
    fun contains(date: LocalDate): Boolean {
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }
    
    /**
     * Check if this period is in the past
     */
    val isPast: Boolean
        get() = endDate.isBefore(LocalDate.now())
    
    /**
     * Check if this period is in the future
     */
    val isFuture: Boolean
        get() = startDate.isAfter(LocalDate.now())
    
    /**
     * Check if this period is current (contains today)
     */
    val isCurrent: Boolean
        get() = contains(LocalDate.now())
    
    /**
     * Get the middle date of this period
     */
    val midDate: LocalDate
        get() = startDate.plusDays(daysInPeriod / 2)
    
    // ==================== PERIOD OPERATIONS ====================
    
    /**
     * Gets the next period of the same type
     */
    fun next(): AccountingPeriod {
        return when (type) {
            PeriodType.MONTHLY -> monthly(
                if (endDate.month == Month.DECEMBER) endDate.year + 1 else endDate.year,
                if (endDate.month == Month.DECEMBER) Month.JANUARY else endDate.month.plus(1)
            )
            PeriodType.QUARTERLY -> {
                val nextQuarter = if (periodNumber == 4) 1 else periodNumber + 1
                val nextYear = if (periodNumber == 4) fiscalYear + 1 else fiscalYear
                quarterly(nextYear, nextQuarter)
            }
            PeriodType.ANNUAL -> annual(endDate.year + 1)
            PeriodType.FISCAL_ANNUAL -> fiscalYear(fiscalYear + 1, fiscalYearStartMonth)
            else -> custom(
                name = "Next $name",
                startDate = endDate.plusDays(1),
                endDate = endDate.plusDays(daysInPeriod + 1)
            )
        }
    }
    
    /**
     * Gets the previous period of the same type
     */
    fun previous(): AccountingPeriod {
        return when (type) {
            PeriodType.MONTHLY -> monthly(
                if (startDate.month == Month.JANUARY) startDate.year - 1 else startDate.year,
                if (startDate.month == Month.JANUARY) Month.DECEMBER else startDate.month.minus(1)
            )
            PeriodType.QUARTERLY -> {
                val prevQuarter = if (periodNumber == 1) 4 else periodNumber - 1
                val prevYear = if (periodNumber == 1) fiscalYear - 1 else fiscalYear
                quarterly(prevYear, prevQuarter)
            }
            PeriodType.ANNUAL -> annual(startDate.year - 1)
            PeriodType.FISCAL_ANNUAL -> fiscalYear(fiscalYear - 1, fiscalYearStartMonth)
            else -> custom(
                name = "Previous $name",
                startDate = startDate.minusDays(daysInPeriod),
                endDate = startDate.minusDays(1)
            )
        }
    }
    
    /**
     * Gets the same period last year
     */
    fun samePeriodLastYear(): AccountingPeriod {
        return when (type) {
            PeriodType.MONTHLY -> monthly(startDate.year - 1, startDate.month)
            PeriodType.QUARTERLY -> quarterly(fiscalYear - 1, periodNumber)
            PeriodType.ANNUAL -> annual(startDate.year - 1)
            PeriodType.FISCAL_ANNUAL -> fiscalYear(fiscalYear - 1, fiscalYearStartMonth)
            else -> custom(
                name = "$name (Prior Year)",
                startDate = startDate.minusYears(1),
                endDate = endDate.minusYears(1)
            )
        }
    }
    
    /**
     * Splits this period into sub-periods
     */
    fun splitInto(subPeriodType: PeriodType): List<AccountingPeriod> {
        return when (subPeriodType) {
            PeriodType.MONTHLY -> splitIntoMonths()
            PeriodType.WEEKLY -> splitIntoWeeks()
            PeriodType.DAILY -> splitIntoDays()
            else -> listOf(this)
        }
    }
    
    private fun splitIntoMonths(): List<AccountingPeriod> {
        val periods = mutableListOf<AccountingPeriod>()
        var current = startDate.withDayOfMonth(1)
        
        while (!current.isAfter(endDate)) {
            val monthEnd = current.withDayOfMonth(current.lengthOfMonth())
            val periodStart = maxOf(current, startDate)
            val periodEnd = minOf(monthEnd, endDate)
            
            periods.add(monthly(current.year, current.month))
            current = current.plusMonths(1)
        }
        
        return periods
    }
    
    private fun splitIntoWeeks(): List<AccountingPeriod> {
        val periods = mutableListOf<AccountingPeriod>()
        var current = startDate
        
        while (!current.isAfter(endDate)) {
            val weekEnd = minOf(current.plusDays(6), endDate)
            periods.add(
                custom(
                    name = "Week of ${current.format(DateTimeFormatter.ofPattern("MMM dd"))}",
                    startDate = current,
                    endDate = weekEnd
                )
            )
            current = weekEnd.plusDays(1)
        }
        
        return periods
    }
    
    private fun splitIntoDays(): List<AccountingPeriod> {
        val periods = mutableListOf<AccountingPeriod>()
        var current = startDate
        
        while (!current.isAfter(endDate)) {
            periods.add(
                custom(
                    name = current.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    startDate = current,
                    endDate = current
                )
            )
            current = current.plusDays(1)
        }
        
        return periods
    }
    
    /**
     * Checks if this period overlaps with another period
     */
    fun overlaps(other: AccountingPeriod): Boolean {
        return !(endDate.isBefore(other.startDate) || startDate.isAfter(other.endDate))
    }
    
    /**
     * Gets the overlap period with another period
     */
    fun overlap(other: AccountingPeriod): AccountingPeriod? {
        if (!overlaps(other)) return null
        
        val overlapStart = maxOf(startDate, other.startDate)
        val overlapEnd = minOf(endDate, other.endDate)
        
        return custom(
            name = "Overlap Period",
            startDate = overlapStart,
            endDate = overlapEnd,
            description = "Overlap between '$name' and '${other.name}'"
        )
    }
    
    /**
     * Extends this period by the specified number of days
     */
    fun extend(days: Int): AccountingPeriod {
        return copy(endDate = endDate.plusDays(days.toLong()))
    }
    
    /**
     * Shortens this period by the specified number of days
     */
    fun shorten(days: Int): AccountingPeriod {
        val newEndDate = endDate.minusDays(days.toLong())
        require(!newEndDate.isBefore(startDate)) { "Cannot shorten period below start date" }
        return copy(endDate = newEndDate)
    }
    
    // ==================== COMPARISON OPERATIONS ====================
    
    override fun compareTo(other: AccountingPeriod): Int {
        val startComparison = startDate.compareTo(other.startDate)
        return if (startComparison != 0) {
            startComparison
        } else {
            endDate.compareTo(other.endDate)
        }
    }
    
    /**
     * Checks if this period is before another period
     */
    fun isBefore(other: AccountingPeriod): Boolean {
        return endDate.isBefore(other.startDate)
    }
    
    /**
     * Checks if this period is after another period
     */
    fun isAfter(other: AccountingPeriod): Boolean {
        return startDate.isAfter(other.endDate)
    }
    
    // ==================== BUSINESS LOGIC ====================
    
    /**
     * Closes this accounting period
     */
    fun close(): AccountingPeriod {
        return copy(isClosed = true, isActive = false)
    }
    
    /**
     * Reopens this accounting period
     */
    fun reopen(): AccountingPeriod {
        return copy(isClosed = false, isActive = true)
    }
    
    /**
     * Marks this as an adjustment period
     */
    fun markAsAdjustmentPeriod(): AccountingPeriod {
        return copy(isAdjustmentPeriod = true)
    }
    
    /**
     * Gets a formatted display string
     */
    fun getDisplayString(): String {
        return "$name (${startDate.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))})"
    }
    
    /**
     * Gets period status summary
     */
    fun getStatusSummary(): String {
        return buildString {
            append(name)
            when {
                isClosed -> append(" [CLOSED]")
                !isActive -> append(" [INACTIVE]")
                isAdjustmentPeriod -> append(" [ADJUSTMENT]")
                isCurrent -> append(" [CURRENT]")
                isPast -> append(" [PAST]")
                isFuture -> append(" [FUTURE]")
            }
        }
    }
    
    // ==================== VALIDATION ====================
    
    init {
        require(!endDate.isBefore(startDate)) { "End date cannot be before start date" }
        require(name.isNotBlank()) { "Period name cannot be blank" }
        require(daysInPeriod <= 366) { "Period cannot exceed 366 days" }
        require(periodNumber >= 1) { "Period number must be at least 1" }
        require(fiscalYear >= 1) { "Fiscal year must be at least 1" }
    }
}

// ==================== EXTENSION FUNCTIONS ====================

/**
 * Extension functions for PeriodType enum
 */
fun PeriodType.getDisplayName(): String = when (this) {
    PeriodType.DAILY -> "Daily"
    PeriodType.WEEKLY -> "Weekly"
    PeriodType.MONTHLY -> "Monthly"
    PeriodType.QUARTERLY -> "Quarterly"
    PeriodType.SEMI_ANNUAL -> "Semi-Annual"
    PeriodType.ANNUAL -> "Annual"
    PeriodType.FISCAL_MONTHLY -> "Fiscal Monthly"
    PeriodType.FISCAL_QUARTERLY -> "Fiscal Quarterly"
    PeriodType.FISCAL_SEMI_ANNUAL -> "Fiscal Semi-Annual"
    PeriodType.FISCAL_ANNUAL -> "Fiscal Annual"
    PeriodType.CUSTOM -> "Custom"
    PeriodType.ROLLING_PERIOD -> "Rolling Period"
    PeriodType.PROJECT_PERIOD -> "Project Period"
    PeriodType.SEASONAL_PERIOD -> "Seasonal Period"
    PeriodType.YEAR_TO_DATE -> "Year to Date"
    PeriodType.QUARTER_TO_DATE -> "Quarter to Date"
    PeriodType.MONTH_TO_DATE -> "Month to Date"
    PeriodType.WEEK_TO_DATE -> "Week to Date"
    PeriodType.PRIOR_YEAR -> "Prior Year"
    PeriodType.PRIOR_QUARTER -> "Prior Quarter"
    PeriodType.PRIOR_MONTH -> "Prior Month"
    PeriodType.SAME_PERIOD_LAST_YEAR -> "Same Period Last Year"
}

fun PeriodType.isStandardPeriod(): Boolean = when (this) {
    PeriodType.DAILY,
    PeriodType.WEEKLY,
    PeriodType.MONTHLY,
    PeriodType.QUARTERLY,
    PeriodType.SEMI_ANNUAL,
    PeriodType.ANNUAL -> true
    else -> false
}

fun PeriodType.isFiscalPeriod(): Boolean = when (this) {
    PeriodType.FISCAL_MONTHLY,
    PeriodType.FISCAL_QUARTERLY,
    PeriodType.FISCAL_SEMI_ANNUAL,
    PeriodType.FISCAL_ANNUAL -> true
    else -> false
}

fun PeriodType.isToDatePeriod(): Boolean = when (this) {
    PeriodType.YEAR_TO_DATE,
    PeriodType.QUARTER_TO_DATE,
    PeriodType.MONTH_TO_DATE,
    PeriodType.WEEK_TO_DATE -> true
    else -> false
}

fun PeriodType.isComparativePeriod(): Boolean = when (this) {
    PeriodType.PRIOR_YEAR,
    PeriodType.PRIOR_QUARTER,
    PeriodType.PRIOR_MONTH,
    PeriodType.SAME_PERIOD_LAST_YEAR -> true
    else -> false
}
