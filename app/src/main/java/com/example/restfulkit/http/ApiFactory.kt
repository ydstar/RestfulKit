package com.example.restfulkit.http

import com.restful.kit.IRestful


/**
 * Author: 信仰年轻
 * Date: 2020-11-05 15:51
 * Email: hydznsqk@163.com
 * Des:
 */
object ApiFactory {


    val HTTPS_BASE_URL = "https://api.devio.org/as/"

    val baseUrl = HTTPS_BASE_URL
    private val iRestful: IRestful = IRestful(baseUrl, RetrofitCallFactory(baseUrl))

    init {
        iRestful.addInterceptor(BizInterceptor())
        iRestful.addInterceptor(HttpCodeInterceptor())

    }

    fun <T> create(service: Class<T>): T {
        return iRestful.create(service)
    }
}