package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey
    val id: String, // Member ID (e.g. "M-101", "আলী আহমেদ", etc.)
    val initialPrincipal: Long,
    val phone: String = "",
    val photoUri: String = "",
    val guarantor: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val startDate: Long = System.currentTimeMillis()
)
