package com.restful.kit.cache

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Author: 信仰年轻
 * Date: 2020-12-25 16:00
 * Email: hydznsqk@163.com
 * Des: 表结构
 */
@Entity(tableName = "cache")
class Cache {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    var key: String = ""

    var data: ByteArray? = null
}