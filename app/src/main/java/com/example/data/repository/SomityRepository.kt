package com.example.data.repository

import com.example.data.db.SomityDao
import com.example.data.model.MemberEntity
import com.example.data.model.MemberWithWeeks
import com.example.data.model.WeekRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SomityRepository(private val dao: SomityDao) {

    val allMembers: Flow<List<MemberWithWeeks>> = dao.getAllMembersWithWeeks()

    suspend fun addMember(
        id: String,
        initialPrincipal: Long,
        phone: String = "",
        photoUri: String = "",
        guarantor: String = "",
        startDate: Long = System.currentTimeMillis()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmedId = id.trim()
        if (trimmedId.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Member ID খালি হতে পারে না"))
        }
        val existing = dao.findMemberById(trimmedId)
        if (existing != null) {
            return@withContext Result.failure(IllegalStateException("এই Member ID আগে থেকেই আছে"))
        }
        dao.insertMember(
            MemberEntity(
                id = trimmedId,
                initialPrincipal = initialPrincipal,
                phone = phone.trim(),
                photoUri = photoUri.trim(),
                guarantor = guarantor.trim(),
                startDate = startDate
            )
        )
        Result.success(Unit)
    }

    suspend fun updateMemberProfile(
        oldId: String,
        newId: String,
        phone: String,
        photoUri: String,
        guarantor: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmedNewId = newId.trim()
        if (trimmedNewId.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("নাম বা আইডি খালি হতে পারে না"))
        }
        if (trimmedNewId.lowercase() != oldId.trim().lowercase()) {
            val existing = dao.findMemberById(trimmedNewId)
            if (existing != null) {
                return@withContext Result.failure(IllegalStateException("এই নামে বা আইডিতে ইতিমধ্যে অন্য সদস্য আছে"))
            }
        }
        try {
            dao.updateMemberFull(
                oldId = oldId.trim(),
                newId = trimmedNewId,
                phone = phone.trim(),
                photoUri = photoUri.trim(),
                guarantor = guarantor.trim()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addWeekRecord(memberId: String, expectedAmount: Long, receivedAmount: Long, weekIndex: Int? = null) = withContext(Dispatchers.IO) {
        val existingMember = dao.findMemberById(memberId) ?: return@withContext
        val targetIndex = if (weekIndex != null && weekIndex > 0) {
            weekIndex
        } else {
            dao.getWeekRecordCountForMember(memberId) + 1
        }
        val record = WeekRecordEntity(
            memberId = memberId,
            weekIndex = targetIndex,
            expectedAmount = expectedAmount,
            receivedAmount = receivedAmount
        )
        dao.insertWeekRecord(record)
    }

    suspend fun deleteMember(member: MemberEntity) = withContext(Dispatchers.IO) {
        dao.deleteMember(member)
    }

    suspend fun deleteWeekRecord(weekId: Long) = withContext(Dispatchers.IO) {
        dao.deleteWeekRecord(weekId)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getMemberCount() == 0) {
            val now = System.currentTimeMillis()
            val oneDayMillis = 24L * 60 * 60 * 1000L

            // Seed 3 realistic members with 7-day cycle dates
            // M-101: Started 14 days ago (2 weeks ago)
            val m1 = MemberEntity(id = "M-101", initialPrincipal = 50000L, startDate = now - (14 * oneDayMillis))
            // M-102: Started 10 days ago (Week 1 elapsed with partial pay, Week 2 active)
            val m2 = MemberEntity(id = "M-102", initialPrincipal = 30000L, startDate = now - (10 * oneDayMillis))
            // M-103: Started 3 days ago (Week 1 active, 4 days remaining)
            val m3 = MemberEntity(id = "M-103", initialPrincipal = 25000L, startDate = now - (3 * oneDayMillis))
            dao.insertMember(m1)
            dao.insertMember(m2)
            dao.insertMember(m3)

            // M-101: 50,000 -> weekly profit = 1,000. 2 weeks paid in full
            dao.insertWeekRecord(WeekRecordEntity(memberId = "M-101", weekIndex = 1, expectedAmount = 1000L, receivedAmount = 1000L))
            dao.insertWeekRecord(WeekRecordEntity(memberId = "M-101", weekIndex = 2, expectedAmount = 1000L, receivedAmount = 1000L))

            // M-102: 30,000 -> weekly profit = 600. Week 1 paid 400 (unpaid 200 added to principal)
            dao.insertWeekRecord(WeekRecordEntity(memberId = "M-102", weekIndex = 1, expectedAmount = 600L, receivedAmount = 400L))
        }
    }
}
