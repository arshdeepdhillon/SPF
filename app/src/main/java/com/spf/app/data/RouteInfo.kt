package com.spf.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routeInfo")
data class RouteInfo(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "routeId")
    val routeId: Long,
    @ColumnInfo(name = "address")
    val address: String,
    @ColumnInfo(name = "optimalIndex", defaultValue = "0")
    val optIndex: Long,
    @ColumnInfo(name = "groupId")
    val groupId: Long,
)
