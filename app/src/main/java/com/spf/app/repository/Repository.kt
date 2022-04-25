package com.spf.app.repository

import androidx.lifecycle.LiveData
import com.spf.app.dao.RouteGroupDao
import com.spf.app.dao.RouteInfoDao
import com.spf.app.data.DataState
import com.spf.app.data.RouteGroup
import com.spf.app.data.RouteInfo

class Repository(private val group: RouteGroupDao, private val route: RouteInfoDao) {
    val allGroups: LiveData<List<RouteGroup>> = group.getAllByState()
    fun allRoutesInGroup(id: Long): LiveData<List<RouteInfo>> = route.allRoutesInGroup(id)

    // Group related

    suspend fun createGroup(data: RouteGroup) = group.create(data)

    suspend fun createGroup(title: String) = group.create(title)

    suspend fun deleteGroup(id: Long) = group.delete(id)

    suspend fun updateGroup(data: RouteGroup) = group.update(data)

    suspend fun updateGroupState(groupId: Long, state: DataState) = group.updateState(groupId, state)

    suspend fun getGroup(id: Long): RouteGroup? = group.get(id)


    // Route related

    suspend fun getRoutesInGroup(id: Long): List<RouteInfo> = route.routesInGroup(id)

    suspend fun createRoute(groupId: Long, address: String, optIndex: Long) = route.create(groupId, address, optIndex)

    suspend fun deleteRoute(routeId: Long) = route.deleteAndUpdateOpt(routeId)

    suspend fun updateRoute(data: RouteInfo) = route.update(data)

    suspend fun getRoute(id: Long): RouteInfo? = route.get(id)

    suspend fun updateRouteAddress(routeId: Long, newAddress: String) {
        route.updateAddress(routeId, newAddress)
    }

    suspend fun updateGroupTitle(groupId: Long, newTitle: String) {
        group.updateTitle(groupId, newTitle)
    }

    suspend fun getRoutesInGroupByOpt(groupId: Long) = route.getRoutesInGroupByOpt(groupId)

    suspend fun getRoutesInGroupByOptWithoutCurrLocation(groupId: Long) = route.getRoutesInGroupByOptWithoutCurrLocation(groupId)

    suspend fun updateRouteOptIndex(routeId: Long, newOptIndex: Long) {
        route.updateOptIndex(routeId, newOptIndex)
    }

    suspend fun updateAddressUiState(groupId: Long) {
        route.updateAddressUiState(groupId)
    }

    suspend fun getLastOptIndex(groupId: Long) = route.lastOptIndex(groupId)

    suspend fun updateOptOnDragDown(fromItem: RouteInfo, toPos: Long) {
        route.updateOptOnDragDown(fromItem, toPos)
    }

    suspend fun updateOptOnDragUp(fromItem: RouteInfo, toPos: Long) {
        route.updateOptOnDragUp(fromItem, toPos)
    }
}