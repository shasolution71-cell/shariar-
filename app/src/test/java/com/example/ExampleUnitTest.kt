package com.example

import com.example.data.model.MemberEntity
import com.example.data.model.MemberWithWeeks
import com.example.data.model.WeekRecordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun newMember_doesNotAddProfitToPrincipalImmediately() {
        val now = System.currentTimeMillis()
        val member = MemberEntity(
            id = "Test-1",
            initialPrincipal = 20000L,
            phone = "01712345678",
            startDate = now
        )
        val memberWithWeeks = MemberWithWeeks(member = member, weeks = emptyList())
        val cycleInfo = memberWithWeeks.getCycleInfo(now)

        // Asol should remain exactly 20000
        assertEquals(20000L, cycleInfo.currentPrincipal)
        // Weekly profit is 20 per 1000 = 400
        assertEquals(400L, cycleInfo.weeklyProfit)
        // Total debt on day 1 is 20000 (no overdue profit)
        assertEquals(20000L, cycleInfo.totalDebt)
        // Week number is 1
        assertEquals(1, cycleInfo.currentWeekNumber)
        // Not overdue
        assertTrue(cycleInfo.isActive)
        assertFalse(cycleInfo.isOverdue)
        // Phone number is preserved
        assertEquals("01712345678", member.phone)
    }

    @Test
    fun payingCurrentWeek_advancesRunningWeekToNextWeek() {
        val now = System.currentTimeMillis()
        val member = MemberEntity(
            id = "Test-2",
            initialPrincipal = 20000L,
            phone = "01812345678",
            startDate = now
        )
        // Record week 1 payment
        val week1 = WeekRecordEntity(
            memberId = "Test-2",
            weekIndex = 1,
            expectedAmount = 400L,
            receivedAmount = 400L
        )
        val memberWithWeeks = MemberWithWeeks(member = member, weeks = listOf(week1))
        val cycleInfo = memberWithWeeks.getCycleInfo(now)

        // Week number should advance to Week 2!
        assertEquals(2, cycleInfo.currentWeekNumber)
        // Since week 1 was fully paid, principal stays 20000
        assertEquals(20000L, cycleInfo.currentPrincipal)
        // Week 2 profit
        assertEquals(400L, cycleInfo.weeklyProfit)
        // Total debt is 20000
        assertEquals(20000L, cycleInfo.totalDebt)
    }
}
