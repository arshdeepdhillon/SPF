package com.spf.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

//@Entity(tableName = "routeInfo", indices = [Index(value = ["optimalIndex", "groupId"], unique = true)])
@Entity(tableName = "routeInfo")
data class RouteInfo(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "routeId")
    val routeId: Long,
    @ColumnInfo(name = "groupId")
    val groupId: Long,
    @ColumnInfo(name = "address")
    val address: String,
    @ColumnInfo(name = "optimalIndex", defaultValue = "0")
    val optIndex: Long,
    @ColumnInfo(name = "dragState", defaultValue = "false")
    val dragState: Boolean,
)

//@Entity(tableName = "optimalRoute",
//    primaryKeys = ["routeId", "groupId"],
//    foreignKeys = [ForeignKey(entity = RouteInfo::class,
//        parentColumns = ["routeId", "groupId"],
//        childColumns = ["routeId", "groupId"],
//        onDelete = ForeignKey.CASCADE)])
//data class OptimalRouteIndex(
//    @ColumnInfo(name = "routeId")
//    val routeId: Long,
//    @ColumnInfo(name = "groupId")
//    val groupId: Long,
//    @ColumnInfo(name = "optimalIndex", defaultValue = "0")
//    val optIndex: Long,
//)

/** To manage the state of RouteInfo item on swipe */
enum class DataState {
    /** Item can be shown in UI */
    SHOW,

    /** Item has been removed/swiped from UI by user so hide it until user's final action */
    HIDE,

    /** Ready to be delete */
    DELETE
}