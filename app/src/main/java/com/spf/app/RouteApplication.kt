package com.spf.app

import android.app.Application
import com.spf.app.database.RouteDB
import com.spf.app.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class RouteApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { RouteDB.getDB(this, applicationScope) }
    val repository by lazy { Repository(database.routeGroupDao(), database.routeInfoDao()) }
}