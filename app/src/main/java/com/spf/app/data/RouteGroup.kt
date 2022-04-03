package com.spf.app.data

import androidx.room.*

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

data class RoutesInGroup(
    @Embedded
    val routeGroup: RouteGroup,
    @Relation(parentColumn = "groupId", entityColumn = "routeId")
    val routes: List<RouteInfo>,
)