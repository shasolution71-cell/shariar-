package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.MemberEntity
import com.example.data.model.MemberWithWeeks
import com.example.data.model.WeekRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SomityDao {

    @Transaction
    @Query("SELECT * FROM members ORDER BY createdAt ASC")
    fun getAllMembersWithWeeks(): Flow<List<MemberWithWeeks>>

    @Transaction
    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    fun getMemberWithWeeks(id: String): Flow<MemberWithWeeks?>

    @Query("SELECT COUNT(*) FROM members")
    suspend fun getMemberCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMember(member: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeekRecord(week: WeekRecordEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    @Query("DELETE FROM week_records WHERE id = :weekId")
    suspend fun deleteWeekRecord(weekId: Long)

    @Query("SELECT * FROM members WHERE LOWER(id) = LOWER(:id) LIMIT 1")
    suspend fun findMemberById(id: String): MemberEntity?

    @Query("SELECT COUNT(*) FROM week_records WHERE memberId = :memberId")
    suspend fun getWeekRecordCountForMember(memberId: String): Int

    @Query("UPDATE members SET phone = :phone, photoUri = :photoUri, guarantor = :guarantor WHERE id = :id")
    suspend fun updateMemberProfile(id: String, phone: String, photoUri: String, guarantor: String)

    @Query("UPDATE week_records SET memberId = :newId WHERE memberId = :oldId")
    suspend fun updateWeekRecordsMemberId(oldId: String, newId: String)

    @Transaction
    suspend fun updateMemberFull(
        oldId: String,
        newId: String,
        phone: String,
        photoUri: String,
        guarantor: String
    ) {
        if (oldId == newId) {
            updateMemberProfile(oldId, phone, photoUri, guarantor)
        } else {
            val oldMember = findMemberById(oldId) ?: return
            val newMember = oldMember.copy(
                id = newId,
                phone = phone,
                photoUri = photoUri,
                guarantor = guarantor
            )
            insertMember(newMember)
            updateWeekRecordsMemberId(oldId, newId)
            deleteMember(oldMember)
        }
    }
}
