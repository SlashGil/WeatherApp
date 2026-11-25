package com.slashgil.weather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ab_tests")
data class ABTestEntity(
    @PrimaryKey
    val name: String,
    val value: Boolean
)
