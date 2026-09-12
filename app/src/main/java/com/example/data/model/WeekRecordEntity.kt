package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "week_records",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["memberId"])]
)
data class WeekRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: String,
    val weekIndex: Int,
    val expectedAmount: Long,
    val receivedAmount: Long,
    val recordedAt: Long = System.currentTimeMillis()
)
