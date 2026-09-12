package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MemberStatus {
    ACTIVE,
    OVERDUE
}

data class CycleInfo(
    val currentWeekNumber: Int,
    val daysRemaining: Int,
    val countdownText: String,
    val banglaCountdownText: String,
    val status: MemberStatus,
    val autoCompoundedWeeksCount: Int,
    val autoCompoundedAmount: Long,
    val manualUnpaidAmount: Long,
    val currentPrincipal: Long,
    val weeklyProfit: Long,
    val totalDebt: Long,
    val isPaidThisWeek: Boolean
) {
    val isOverdue: Boolean get() = status == MemberStatus.OVERDUE
    val isActive: Boolean get() = status == MemberStatus.ACTIVE
    val statusText: String get() = banglaCountdownText
}

data class WeekDisplayItem(
    val weekIndex: Int,
    val expectedAmount: Long,
    val receivedAmount: Long,
    val isAutoCompounded: Boolean,
    val isCurrentActive: Boolean,
    val isPaid: Boolean,
    val dueAmount: Long
)

data class MemberWithWeeks(
    @Embedded
    val member: MemberEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "memberId"
    )
    val weeks: List<WeekRecordEntity> = emptyList()
) {
    /**
     * Calculates 7-day cycle information, countdown, running week,
     * and auto-compounding of unpaid profits into the principal.
     */
    fun getCycleInfo(now: Long = System.currentTimeMillis()): CycleInfo {
        val effectiveStart = if (member.startDate > 0L) member.startDate else member.createdAt
        val elapsedMillis = (now - effectiveStart).coerceAtLeast(0L)
        val oneDayMillis = 24L * 60 * 60 * 1000L
        val oneWeekMillis = 7L * oneDayMillis
        val elapsedWeeksByTime = (elapsedMillis / oneWeekMillis).toInt()

        // Highest week index recorded so far
        val maxRecordedWeek = weeks.maxOfOrNull { it.weekIndex } ?: 0

        // Running week advances whenever previous weeks are recorded or when time elapses
        val currentWeekNumber = maxOf(maxRecordedWeek + 1, elapsedWeeksByTime + 1)

        // 7-day cycle window for the current active week
        val currentCycleStart = effectiveStart + (currentWeekNumber - 1) * oneWeekMillis
        val currentCycleEnd = currentCycleStart + oneWeekMillis
        val remainingMillisInCycle = currentCycleEnd - now

        val rawDays = if (remainingMillisInCycle <= 0L) {
            0
        } else {
            ((remainingMillisInCycle + oneDayMillis - 1) / oneDayMillis).toInt()
        }

        val daysRemaining = when {
            rawDays >= 7 -> 7
            rawDays <= 0 -> 0
            else -> rawDays
        }

        val countdownText = when {
            daysRemaining > 1 -> "$daysRemaining days remaining"
            daysRemaining == 1 -> "1 day remaining"
            else -> "0 days remaining"
        }

        val banglaCountdownText = when {
            daysRemaining > 1 -> "আর $daysRemaining দিন বাকি"
            daysRemaining == 1 -> "আর ১ দিন বাকি"
            else -> "আজই শেষ দিন"
        }

        var runningPrincipal = member.initialPrincipal
        var autoCompoundedAmount = 0L
        var autoCompoundedWeeksCount = 0
        var manualUnpaidAmount = 0L

        // Evaluate all completed past weeks (strictly before currentWeekNumber)
        for (w in 1 until currentWeekNumber) {
            val record = weeks.find { it.weekIndex == w }
            if (record != null) {
                val unpaid = (record.expectedAmount - record.receivedAmount).coerceAtLeast(0L)
                manualUnpaidAmount += unpaid
                runningPrincipal += unpaid
            } else {
                // Past week elapsed without payment record -> auto-compound to principal
                val expectedProfit = (runningPrincipal / 1000L) * 20L
                autoCompoundedAmount += expectedProfit
                autoCompoundedWeeksCount++
                runningPrincipal += expectedProfit
            }
        }

        // Current week's calculated profit (৳20 per ৳1,000 of current principal)
        val currentWeekProfit = (runningPrincipal / 1000L) * 20L
        val currentRecord = weeks.find { it.weekIndex == currentWeekNumber }
        val isPaidThisWeek = currentRecord != null && currentRecord.receivedAmount >= currentRecord.expectedAmount
        val currentWeekDue = if (currentRecord != null) {
            (currentRecord.expectedAmount - currentRecord.receivedAmount).coerceAtLeast(0L)
        } else {
            0L // Ongoing active cycle: profit is not overdue/compounded onto principal yet!
        }

        // Overdue status:
        // 1. Any past week left unpaid (autoCompoundedWeeksCount > 0 or manualUnpaidAmount > 0)
        // 2. OR current cycle has expired (daysRemaining == 0) and not yet paid
        val isOverdue = autoCompoundedWeeksCount > 0 || manualUnpaidAmount > 0 || (daysRemaining == 0 && !isPaidThisWeek)
        val status = if (isOverdue) MemberStatus.OVERDUE else MemberStatus.ACTIVE

        // Total Debt (আসল + বকেয়া লাভ):
        // For a new/active member on track, debt is simply their current principal.
        // If expired or has remaining due on current record, add due profit.
        val totalDebt = if (daysRemaining == 0 && !isPaidThisWeek) {
            runningPrincipal + currentWeekProfit
        } else {
            runningPrincipal + currentWeekDue
        }

        return CycleInfo(
            currentWeekNumber = currentWeekNumber,
            daysRemaining = daysRemaining,
            countdownText = countdownText,
            banglaCountdownText = banglaCountdownText,
            status = status,
            autoCompoundedWeeksCount = autoCompoundedWeeksCount,
            autoCompoundedAmount = autoCompoundedAmount,
            manualUnpaidAmount = manualUnpaidAmount,
            currentPrincipal = runningPrincipal,
            weeklyProfit = currentWeekProfit,
            totalDebt = totalDebt,
            isPaidThisWeek = isPaidThisWeek
        )
    }

    /**
     * Total debt owed by the member (compounded principal + any overdue profit)
     */
    fun totalDebt(): Long = getCycleInfo().totalDebt

    /**
     * Current principal (initial principal + unpaid profits compounded)
     */
    fun currentPrincipal(): Long = getCycleInfo().currentPrincipal

    /**
     * Weekly profit = 20 taka per 1,000 taka of current principal
     */
    fun weeklyProfit(): Long = getCycleInfo().weeklyProfit

    fun totalExpected(): Long {
        val cycle = getCycleInfo()
        return weeks.sumOf { it.expectedAmount } + cycle.autoCompoundedAmount
    }

    fun totalReceived(): Long = weeks.sumOf { it.receivedAmount }

    fun totalUnpaid(): Long {
        val cycle = getCycleInfo()
        return cycle.manualUnpaidAmount + cycle.autoCompoundedAmount
    }

    fun hasUnpaid(): Boolean = totalUnpaid() > 0 || getCycleInfo().autoCompoundedWeeksCount > 0

    fun getAllWeekDisplayItems(now: Long = System.currentTimeMillis()): List<WeekDisplayItem> {
        val cycle = getCycleInfo(now)
        val list = mutableListOf<WeekDisplayItem>()
        var runningPrincipal = member.initialPrincipal

        for (w in 1 until cycle.currentWeekNumber) {
            val record = weeks.find { it.weekIndex == w }
            if (record != null) {
                val isPaid = record.receivedAmount >= record.expectedAmount
                val due = (record.expectedAmount - record.receivedAmount).coerceAtLeast(0L)
                list.add(
                    WeekDisplayItem(
                        weekIndex = w,
                        expectedAmount = record.expectedAmount,
                        receivedAmount = record.receivedAmount,
                        isAutoCompounded = false,
                        isCurrentActive = false,
                        isPaid = isPaid,
                        dueAmount = due
                    )
                )
                runningPrincipal += due
            } else {
                val expected = (runningPrincipal / 1000L) * 20L
                list.add(
                    WeekDisplayItem(
                        weekIndex = w,
                        expectedAmount = expected,
                        receivedAmount = 0L,
                        isAutoCompounded = true,
                        isCurrentActive = false,
                        isPaid = false,
                        dueAmount = expected
                    )
                )
                runningPrincipal += expected
            }
        }

        // Add current active cycle
        val currentExpected = (runningPrincipal / 1000L) * 20L
        val currentRecord = weeks.find { it.weekIndex == cycle.currentWeekNumber }
        val isPaidCurrent = currentRecord != null && currentRecord.receivedAmount >= currentRecord.expectedAmount
        val receivedCurrent = currentRecord?.receivedAmount ?: 0L
        list.add(
            WeekDisplayItem(
                weekIndex = cycle.currentWeekNumber,
                expectedAmount = currentExpected,
                receivedAmount = receivedCurrent,
                isAutoCompounded = false,
                isCurrentActive = true,
                isPaid = isPaidCurrent,
                dueAmount = (currentExpected - receivedCurrent).coerceAtLeast(0L)
            )
        )

        return list
    }

    companion object {
        fun formatMoney(amount: Long): String {
            val formatter = NumberFormat.getNumberInstance(Locale("bn", "BD"))
            return try {
                "৳" + formatter.format(amount)
            } catch (e: Exception) {
                "৳$amount"
            }
        }

        fun formatBanglaDate(millis: Long): String {
            return try {
                val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale("bn", "BD"))
                sdf.format(Date(millis))
            } catch (e: Exception) {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                sdf.format(Date(millis))
            }
        }

        fun formatBanglaDateWithDay(millis: Long): String {
            return try {
                val sdf = SimpleDateFormat("dd MMMM, yyyy (EEEE)", Locale("bn", "BD"))
                sdf.format(Date(millis))
            } catch (e: Exception) {
                val sdf = SimpleDateFormat("dd MMM yyyy (EEE)", Locale.ENGLISH)
                sdf.format(Date(millis))
            }
        }
    }
}
