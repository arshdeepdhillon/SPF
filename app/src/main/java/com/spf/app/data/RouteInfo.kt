package com.spf.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.NO_ACTION
import androidx.room.PrimaryKey

// Not using ForeignKey.CASCADE on child table in case we need to manipulate data on delete (ie: Recycle Bin)
@Entity(foreignKeys = [ForeignKey(
    entity = RouteGroup::class,
    parentColumns = ["groupId"],
    childColumns = ["groupId"],
    onDelete = NO_ACTION)]
)
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

/** To manage the state of RouteInfo item on swipe */
enum class DataState {
    /** Item can be shown in UI */
    SHOW,

    /** Item has been removed/swiped from UI by user so hide it until user's final action */
    HIDE,

    /** Ready to be delete */
    DELETE
}