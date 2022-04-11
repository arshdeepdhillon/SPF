package com.spf.app.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.spf.app.data.RouteInfo

@Dao
interface RouteInfoDao {

    @Query("SELECT r.routeId, r.address, r.groupId, r.optimalIndex, r.dragState FROM routeInfo as r, routeGroup as g where r.address != 'My Location' and  g.groupId = :groupId and r.groupId = :groupId")
    fun allRoutesInGroup(groupId: Long): LiveData<List<RouteInfo>>

    @Query("SELECT r.routeId, r.address, r.groupId,r.optimalIndex, r.dragState FROM routeInfo as r, routeGroup as g where g.groupId = :groupId and r.groupId = :groupId")
    suspend fun routesInGroup(groupId: Long): List<RouteInfo>

    @Query("SELECT * FROM routeInfo")
    fun getAllRoutes(): LiveData<List<RouteInfo>>

    @Query("DELETE from routeInfo")
    suspend fun deleteAll()

    @Query("INSERT INTO routeInfo (groupId, address) VALUES (:groupId, :address)")
    suspend fun create(groupId: Long, address: String)

    @Query("SELECT * FROM routeInfo WHERE routeId = :id")
    suspend fun get(id: Long): RouteInfo?

    @Query("SELECT * FROM routeInfo WHERE groupId = :id")
    suspend fun getByGroupId(id: Long): RouteInfo?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(info: RouteInfo)

    @Query("DELETE from routeInfo where routeId = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE routeInfo SET address = :newAddress WHERE routeId = :routeId")
    suspend fun updateAddress(routeId: Long, newAddress: String)

    @Query("SELECT routeId, address, optimalIndex, groupId, dragState FROM routeInfo WHERE groupId = :groupId and address != '' ORDER BY optimalIndex ASC")
    suspend fun getRoutesInGroupByOpt(groupId: Long): List<RouteInfo>

    @Query("SELECT routeId, address, optimalIndex, groupId, dragState FROM routeInfo WHERE groupId = :groupId and address != '' and address != 'My Location' ORDER BY optimalIndex ASC")
    suspend fun getRoutesInGroupByOptWithoutCurrLocation(groupId: Long): List<RouteInfo>

    @Query("UPDATE routeInfo SET optimalIndex = :newOptimalIndex WHERE routeId = :routeId")
    suspend fun updateOptIndex(routeId: Long, newOptimalIndex: Long)

    @Query("UPDATE routeInfo SET dragState = NOT dragState where groupId = :groupId")
    suspend fun updateAddressUiState(groupId: Long)
}