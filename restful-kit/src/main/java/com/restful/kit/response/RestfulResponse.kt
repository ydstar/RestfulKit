package com.restful.kit.response

/**
 * Author: 信仰年轻
 * Date: 2020-11-03 14:49
 * Email: hydznsqk@163.com
 * Des: 响应报文的包装类对象
 */
open class RestfulResponse<T> {


    companion object{
        const val SUCCESS = 0
        const val CACHE_SUCCESS: Int = 304

    }

    //业务状态码
    var code = 0
    //业务数据
    var data: T? = null
    //错误信息
    var msg: String? = null

    //原始数据
    var rawData: String? = null

    //错误状态下的数据
    var errorData: Map<String, String>? = null

    /**
     * 是否请求成功
     */
    fun successful(): Boolean {
        return code == SUCCESS || code == CACHE_SUCCESS
    }

}