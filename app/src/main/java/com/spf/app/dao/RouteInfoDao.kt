package com.spf.app.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.spf.app.data.RouteInfo
import java.util.*

@Dao
interface RouteInfoDao {

    @Query("SELECT routeId, address, groupId, optimalIndex, dragState FROM routeInfo WHERE address != 'My Location' AND groupId = :groupId AND groupId = :groupId ORDER BY optimalIndex ASC")
    fun allRoutesInGroup(groupId: Long): LiveData<List<RouteInfo>>

    @Query("SELECT r.routeId, r.address, r.groupId,r.optimalIndex, r.dragState FROM routeInfo AS r, routeGroup AS g WHERE  g.groupId = :groupId AND r.groupId = :groupId")
    suspend fun routesInGroup(groupId: Long): List<RouteInfo>

    @Query("SELECT routeId, address, optimalIndex, groupId, dragState FROM routeInfo WHERE groupId = :groupId and address != '' ORDER BY optimalIndex ASC")
    suspend fun getRoutesInGroupByOpt(groupId: Long): List<RouteInfo>

    @Query("SELECT routeId, address, optimalIndex, groupId, dragState FROM routeInfo WHERE groupId = :groupId and address != '' and address != 'My Location' ORDER BY optimalIndex ASC")
    suspend fun getRoutesInGroupByOptWithoutCurrLocation(groupId: Long): List<RouteInfo>

    @Query("SELECT * FROM routeInfo")
    fun getAllRoutes(): LiveData<List<RouteInfo>>

    @Query("DELETE FROM routeInfo")
    suspend fun deleteAll()

    @Query("INSERT INTO routeInfo (groupId, address, optimalIndex) VALUES (:groupId, :address, :optIndex)")
    suspend fun create(groupId: Long, address: String, optIndex: Long)

    @Query("SELECT * FROM routeInfo WHERE routeId = :id")
    suspend fun get(id: Long): RouteInfo?

    @Query("SELECT * FROM routeInfo WHERE groupId = :id")
    suspend fun getByGroupId(id: Long): RouteInfo?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(info: RouteInfo)

    @Query("DELETE FROM routeInfo WHERE routeId = :routeId")
    suspend fun delete(routeId: Long)

    @Query("UPDATE routeInfo SET optimalIndex = optimalIndex - 1 WHERE groupId = :groupId AND optimalIndex > :optIndex ")
    suspend fun updateOptOnDelete(groupId: Long, optIndex: Long)

    @Transaction
    suspend fun deleteAndUpdateOpt(routeId: Long) {
        val route = get(routeId)
        //Ignore double click. TODO: fix it in View layer?
        if (route != null) {
            delete(routeId)
            updateOptOnDelete(route.groupId, route.optIndex)
        }
    }

    @Query("UPDATE routeInfo SET address = :newAddress WHERE routeId = :routeId")
    suspend fun updateAddress(routeId: Long, newAddress: String)

    @Query("UPDATE routeInfo SET optimalIndex = :newOptimalIndex WHERE routeId = :routeId")
    suspend fun updateOptIndex(routeId: Long, newOptimalIndex: Long)

    @Query("UPDATE routeInfo SET dragState = NOT dragState where groupId = :groupId")
    suspend fun updateAddressUiState(groupId: Long)

    @Query("SELECT MAX(optimalIndex) FROM routeInfo WHERE groupId = :groupId")
    suspend fun lastOptIndex(groupId: Long): Optional<Long>

    @Query("UPDATE routeInfo SET optimalIndex = :newOptIndex, dragState = NOT dragState WHERE groupId = :groupId AND routeId = :routeId")
    suspend fun updateOptIndexAndUiState(groupId: Long, routeId: Long, newOptIndex: Long)

    @Query("SELECT optimalIndex FROM routeInfo where groupId = :groupId AND optimalIndex = :optIndex ")
    suspend fun getOptIndex(groupId: Long, optIndex: Long): Long

    @Query("UPDATE routeInfo set optimalIndex = optimalIndex - 1 WHERE groupId = :groupId AND optimalIndex > :fromPosOptIndex AND optimalIndex <= :toPosOptIndex")
    suspend fun updateOptOnDragDownHelper(groupId: Long, fromPosOptIndex: Long, toPosOptIndex: Long)

    @Transaction
    suspend fun updateOptOnDragDown(fromItem: RouteInfo, toItem: Long) {
        // Update optimalIndex of all rows between fromItem.optIndex and toItem
        updateOptOnDragDownHelper(fromItem.groupId, fromItem.optIndex, toItem)
        updateOptIndex(fromItem.routeId, toItem)
    }

    @Query("UPDATE routeInfo set optimalIndex = optimalIndex + 1 WHERE groupId = :groupId AND  optimalIndex < :fromPosOptIndex AND optimalIndex >= :toPosOptIndex")
    suspend fun updateOptOnDragUpHelper(groupId: Long, fromPosOptIndex: Long, toPosOptIndex: Long)

    @Transaction
    suspend fun updateOptOnDragUp(fromItem: RouteInfo, toItem: Long) {
        updateOptOnDragUpHelper(fromItem.groupId, fromItem.optIndex, toItem)
        updateOptIndex(fromItem.routeId, toItem)
    }
}