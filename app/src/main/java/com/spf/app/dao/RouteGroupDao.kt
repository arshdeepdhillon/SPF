package com.spf.app.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.lifecycle.LiveData
import com.spf.app.data.RouteGroup

@Dao
interface RouteGroupDao {
    @Query("SELECT * FROM routeGroup")
    fun getAll(): LiveData<List<RouteGroup>>

    @Query("DELETE from routeGroup")
    suspend fun deleteAll()

    @Insert
    suspend fun create(data: RouteGroup): Long

    @Query("SELECT * FROM routeGroup WHERE groupId = :id")
    suspend fun get(id: Long): RouteGroup?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(data: RouteGroup)

    @Query("DELETE from routeGroup where groupId = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE routeGroup SET title = :newTitle WHERE groupId = :groupId")
    suspend fun updateTitle(groupId: Long, newTitle: String)
}