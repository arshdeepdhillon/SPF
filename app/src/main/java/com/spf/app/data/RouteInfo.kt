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
    @ColumnInfo(name = "dragState", defaultValue = "false")
    val dragState: Boolean,
)

/** To manage the state of RouteInfo item on swipe */
enum class DataState {
    /** Item can be shown in UI */
    SHOW,

    /** Item has been removed/swiped from UI by user so hide it until user's final action */
    HIDE,

    /** Ready to be delete */
    DELETE
}