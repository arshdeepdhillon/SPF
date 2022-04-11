package com.spf.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routeGroup")
data class RouteGroup(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "groupId")
    val groupId: Long,
    @ColumnInfo(name = "title")
    val title: String,

//    @ColumnInfo(name = "num_of_waypoints")
//    val numOfWayPoints: Int,
//    @ColumnInfo(name = "last_modified")
//    val lastModified: String,
//    @ColumnInfo(name = "pinned")
//    val pinned: Boolean,
)