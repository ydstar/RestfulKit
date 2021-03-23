package com.restful.kit.cache

import androidx.room.*

/**
 * Author: 信仰年轻
 * Date: 2020-12-25 16:03
 * Email: hydznsqk@163.com
 * Des: 数据库的表操作dao对象
 */
@Dao
interface CacheDao {

    @Insert(entity = Cache::class, onConflict = OnConflictStrategy.REPLACE)
    fun saveCache(cache: Cache): Long

    @Query("select * from cache where `key`=:key")
    fun getCache(key: String): Cache?

    @Delete(entity = Cache::class)
    fun deleteCache(cache: Cache)
}