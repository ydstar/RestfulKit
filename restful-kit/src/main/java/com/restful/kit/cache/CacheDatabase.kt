package com.restful.kit.cache

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.restful.kit.util.AppGlobals


/**
 * Author: 信仰年轻
 * Date: 2020-12-25 16:03
 * Email: hydznsqk@163.com
 * Des: 数据库
 */
@Database(entities = [Cache::class], version = 1)
abstract class CacheDatabase :RoomDatabase() {

    companion object {
        private var database: CacheDatabase
        fun get(): CacheDatabase {
            return database
        }

        init {
            val context = AppGlobals.get()!!.applicationContext
            database =Room.databaseBuilder(context, CacheDatabase::class.java, "i_cache").build()
        }
    }

    abstract val cacheDao: CacheDao
}