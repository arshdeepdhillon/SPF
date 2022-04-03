package com.spf.app.viewModel

import androidx.lifecycle.*
import com.spf.app.data.RouteGroup
import com.spf.app.data.RouteInfo
import com.spf.app.repository.Repository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RouteVM(private val repo: Repository) : ViewModel() {

    private val routeGroupIdLiveData = MutableLiveData<Long>()
    val allRoutesInGroup: LiveData<List<RouteInfo>> =
        Transformations.switchMap(routeGroupIdLiveData) { groupId ->
            repo.allRoutesInGroup(groupId)
        }

    sealed class VMEvent {
        data class RouteInfoEvent(val routeInfoList: List<RouteInfo>) : VMEvent()
        data class RouteGroupEvent(val routeGroup: RouteGroup) : VMEvent()
    }

    private val vmEventChannel = Channel<VMEvent>()
    val vmEventFlow = vmEventChannel.receiveAsFlow()
    fun setRouteGroupId(id: Long) {
        routeGroupIdLiveData.value = id
    }

    val allGroups: LiveData<List<RouteGroup>> = repo.allGroups.asFlow().asLiveData()

    fun triggerInitRoutesInGroupEvent() = viewModelScope.launch {
        val routeInfoData = repo.getRoutesInGroup(routeGroupIdLiveData.value!!)
//        val routeGroupData = repo.getGroup(routeGroupIdLiveData.value!!)
//        routeGroupData?.let { vmEventChannel.send(VMEvent.RouteGroupEvent(it)) }
        vmEventChannel.send(VMEvent.RouteInfoEvent(routeInfoData))
    }


    suspend fun createGroup(data: RouteGroup) = repo.createGroup(data)

    suspend fun getGroup(id: Long) = repo.getGroup(id)

    fun updateGroup(data: RouteGroup) = viewModelScope.launch {
        repo.updateGroup(data)
    }

    //TODO create transaction to delete routes and group
    fun deleteGroup(groupId: Long) = viewModelScope.launch {
        repo.deleteGroup(groupId)
    }

    fun createRoute(data: RouteInfo) = viewModelScope.launch {
        repo.createRoute(data)
    }

    suspend fun getRoutesInGroupByOpt(groupId: Long) = repo.getRoutesInGroupByOpt(groupId)
    suspend fun getRoutesInGroupByOptWithoutCurrLocation(groupId: Long) = repo.getRoutesInGroupByOptWithoutCurrLocation(groupId)

    fun getRoute(id: Long) = viewModelScope.launch {
        repo.getRoute(id)
    }

    fun updateRoute(data: RouteInfo) = viewModelScope.launch {
        repo.updateRoute(data)
    }

    //TODO create transaction to delete routes and group
    fun deleteRoute(routeId: Long) = viewModelScope.launch {
        repo.deleteRoute(routeId)
    }

    fun updateRouteAddress(routeId: Long, newAddress: String) = viewModelScope.launch {
        repo.updateRouteAddress(routeId, newAddress)
    }

    fun updateGroupTitle(groupId: Long, newTitle: String) = viewModelScope.launch {
        repo.updateGroupTitle(groupId, newTitle)
    }

    fun updateOpt(routeId: Long, newOptimalIndex: Long) = viewModelScope.launch {
        repo.updateRouteOptIndex(routeId, newOptimalIndex)
    }

}

class RouteVMFactory(private val repo: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RouteVM::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RouteVM(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}