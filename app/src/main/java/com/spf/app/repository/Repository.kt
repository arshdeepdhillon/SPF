package com.spf.app.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.spf.app.dao.RouteGroupDao
import com.spf.app.dao.RouteInfoDao
import com.spf.app.data.RouteGroup
import com.spf.app.data.RouteInfo

class Repository(private val group: RouteGroupDao, private val route: RouteInfoDao) {
    val allGroups: LiveData<List<RouteGroup>> = group.getAll()
    fun allRoutesInGroup(id: Long): LiveData<List<RouteInfo>> = route.allRoutesInGroup(id)

    // Group related

    @WorkerThread
    suspend fun createGroup(data: RouteGroup) = group.create(data)

    @WorkerThread
    suspend fun deleteGroup(id: Long) = group.delete(id)

    @WorkerThread
    suspend fun updateGroup(data: RouteGroup) = group.update(data)

    @WorkerThread
    suspend fun getGroup(id: Long): RouteGroup? = group.get(id)


    // Route related

    @WorkerThread
    suspend fun getRoutesInGroup(id: Long): List<RouteInfo> = route.routesInGroup(id)

    @WorkerThread
    suspend fun createRoute(groupId: Long, address: String) = route.create(groupId, address)

    @WorkerThread
    suspend fun deleteRoute(id: Long) = route.delete(id)

    @WorkerThread
    suspend fun updateRoute(data: RouteInfo) = route.update(data)

    @WorkerThread
    suspend fun getRoute(id: Long): RouteInfo? = route.get(id)

    @WorkerThread
    suspend fun updateRouteAddress(routeId: Long, newAddress: String) {
        route.updateAddress(routeId, newAddress)
    }

    @WorkerThread
    suspend fun updateGroupTitle(groupId: Long, newTitle: String) {
        group.updateTitle(groupId, newTitle)
    }

    @WorkerThread
    suspend fun getRoutesInGroupByOpt(groupId: Long) = route.getRoutesInGroupByOpt(groupId)

    @WorkerThread
    suspend fun getRoutesInGroupByOptWithoutCurrLocation(groupId: Long) =
        route.getRoutesInGroupByOptWithoutCurrLocation(groupId)


    @WorkerThread
    suspend fun updateRouteOptIndex(routeId: Long, newOptimalIndex: Long) {
        route.updateOptIndex(routeId, newOptimalIndex)
    }

    @WorkerThread
    suspend fun updateAddressUiState(groupId: Long) {
        route.updateAddressUiState(groupId)
    }
}