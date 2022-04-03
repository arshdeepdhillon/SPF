package com.spf.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spf.app.dao.RouteGroupDao
import com.spf.app.dao.RouteInfoDao
import com.spf.app.data.RouteGroup
import com.spf.app.data.RouteInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [RouteGroup::class, RouteInfo::class], version = 1)
abstract class RouteDB : RoomDatabase() {
    abstract fun routeGroupDao(): RouteGroupDao
    abstract fun routeInfoDao(): RouteInfoDao

    companion object {
        @Volatile
        private var INSTANCE: RouteDB? = null
        fun getDB(context: Context, scope: CoroutineScope): RouteDB {
            if (INSTANCE == null) {
                synchronized(this) {
                    val instance = Room.databaseBuilder(context, RouteDB::class.java, "route_db")
                        .addCallback(RouteDBCallback(scope)).build()
                    INSTANCE = instance
                }
            }
            return INSTANCE!!
        }
    }

    private class RouteDBCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    database.routeGroupDao().deleteAll()
                    database.routeInfoDao().deleteAll()
                    (1 until 21).forEach { index ->
                        val groupId: Long = database.routeGroupDao().create(
                            RouteGroup(0, "Route Group #$index")
                        )
//                        arrayOf("My Location",
//                            "5877 Grousewoods Dr, North Vancouver, BC",
//                            "83 Broadway St W, Nakusp, BC",
//                            "2911 Weather Hill, West Kelowna, BC",
//                            "403 Eveline St, Selkirk, MB")
                        arrayOf(
                            "My Location",
                            "140 Harvest Wood Way NE",
                            "140 Taralake Terrace NE",
                            "247 Martinvalley Crescent NE",
                            "113 Tarington Park NE",
                            "19 Savanna St NE",
                            "5215 44 Ave NE"
                        )
                            .forEachIndexed { index, address ->

                                database.routeInfoDao().create(
                                    RouteInfo(0L,
                                        address,
                                        if (address == "My Location") 0L else index.toLong(),
                                        groupId)
                                )
                            }
                    }
                }
            }
        }
    }
}