package com.spf.app.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import androidx.lifecycle.ViewModelProvider
import com.spf.app.data.DataState
import com.spf.app.data.RouteGroup
import com.spf.app.data.RouteInfo
import com.spf.app.repository.Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RouteVM(
    private val repo: Repository,
    private val dispatcher: CoroutineDispatcher,
) :
    ViewModel() {

    private val routeGroupIdLiveData = MutableLiveData<Long>()

    val allRoutesInGroup: LiveData<List<RouteInfo>> =
        Transformations.switchMap(routeGroupIdLiveData) { groupId ->
            repo.allRoutesInGroup(groupId)
        }

    fun setGroupIdOfCurrRoute(id: Long) {
        routeGroupIdLiveData.value = id
    }

    val allGroups: LiveData<List<RouteGroup>> = repo.allGroups.asFlow().asLiveData()

    // TODO improve this logic
    suspend fun updateAddressUiState(groupId: Long) {
        repo.updateAddressUiState(groupId)
    }

    suspend fun createGroup(data: RouteGroup) = repo.createGroup(data)
    suspend fun createGroup(title: String) = repo.createGroup(title)

    suspend fun getGroup(id: Long) = repo.getGroup(id)

    fun updateGroup(data: RouteGroup) = viewModelScope.launch(dispatcher) {
        repo.updateGroup(data)
    }

    fun updateGroupState(groupId: Long, state: DataState) = viewModelScope.launch(dispatcher) {
        if (state == DataState.DELETE)
            repo.deleteGroup(groupId)
        else
            repo.updateGroupState(groupId, state)
    }

    fun deleteGroup(groupId: Long) = viewModelScope.launch(dispatcher) {
        repo.deleteGroup(groupId)
    }

    fun createRoute(groupId: Long, address: String, optIndex: Long = 0L) = viewModelScope.launch(dispatcher) {
        repo.createRoute(groupId, address, optIndex)
    }

    suspend fun getRoutesInGroupByOpt(groupId: Long) = repo.getRoutesInGroupByOpt(groupId)
    suspend fun getRoutesInGroupByOptWithoutCurrLocation(groupId: Long) =
        repo.getRoutesInGroupByOptWithoutCurrLocation(groupId)

    fun getRoute(id: Long) = viewModelScope.launch(dispatcher) {
        repo.getRoute(id)
    }

    fun updateRoute(data: RouteInfo) = viewModelScope.launch(dispatcher) {
        repo.updateRoute(data)
    }

    suspend fun getLastOptIndex() = repo.getLastOptIndex()

    fun deleteRoute(routeId: Long) = viewModelScope.launch(dispatcher) {
        repo.deleteRoute(routeId)
    }

    fun updateRouteAddress(routeId: Long, newAddress: String) = viewModelScope.launch(dispatcher) {
        repo.updateRouteAddress(routeId, newAddress)
    }

    fun updateGroupTitle(groupId: Long, newTitle: String) = viewModelScope.launch(dispatcher) {
        repo.updateGroupTitle(groupId, newTitle)
    }

    fun updateOptIndex(routeId: Long, newOptIndex: Long) = viewModelScope.launch(dispatcher) {
        repo.updateRouteOptIndex(routeId, newOptIndex)
    }

    fun updateOptIndex(routeIdA: Long, optIndexA: Long, routeIdB: Long, optIndexB: Long) = viewModelScope.launch(dispatcher) {
        repo.updateRouteOptIndex(routeIdA, optIndexA, routeIdB, optIndexB)
    }

    suspend fun updateOptOnDragDown(fromItem: RouteInfo, toItem: RouteInfo) {
        repo.updateOptOnDragDown(fromItem, toItem)
    }

    suspend fun updateOptOnDragUp(fromItem: RouteInfo, toItem: RouteInfo) {
        repo.updateOptOnDragUp(fromItem, toItem)
    }
}

class RouteVMFactory(private val repo: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RouteVM::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RouteVM(repo, Dispatchers.IO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}