package com.spf.app.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.lifecycle.LiveData
import androidx.room.Transaction
import com.spf.app.data.DataState
import com.spf.app.data.RouteGroup

@Dao
interface RouteGroupDao {
    @Query("SELECT * FROM routeGroup")
    fun getAll(): LiveData<List<RouteGroup>>

    @Query("SELECT * FROM routeGroup WHERE state = :state")
    fun getAllByState(state: DataState = DataState.SHOW): LiveData<List<RouteGroup>>

    @Query("DELETE from routeGroup")
    suspend fun deleteAll()

    @Insert
    suspend fun create(data: RouteGroup): Long

    @Query("INSERT INTO routeGroup (title, state) VALUES (:title, :state)")
    suspend fun create(title: String, state: DataState = DataState.SHOW): Long

    @Query("SELECT * FROM routeGroup WHERE groupId = :id")
    suspend fun get(id: Long): RouteGroup?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(data: RouteGroup)

    @Query("UPDATE routeGroup SET state = :state WHERE groupId = :id")
    suspend fun updateState(id: Long, state: DataState)

    @Query("DELETE from routeInfo where groupId = :id")
    suspend fun deleteRoutesInGroup(id: Long)

    @Query("DELETE from routeGroup where groupId = :id")
    suspend fun deleteGroup(id: Long)

    @Transaction
    suspend fun delete(id: Long) {
        deleteRoutesInGroup(id)
        deleteGroup(id)
    }

    @Query("UPDATE routeGroup SET title = :newTitle WHERE groupId = :groupId")
    suspend fun updateTitle(groupId: Long, newTitle: String)
}