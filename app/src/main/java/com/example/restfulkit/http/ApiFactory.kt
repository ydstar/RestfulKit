package com.example.restfulkit.http


import com.restful.kit.RestfulKit


/**
 * Author: 信仰年轻
 * Date: 2020-11-05 15:51
 * Email: hydznsqk@163.com
 * Des:
 */
object ApiFactory {

    private val mBaseUrl = "https://api.github.com/"

    private val mRestfulKit: RestfulKit = RestfulKit(mBaseUrl, RetrofitCallFactory(mBaseUrl))

    init {
        mRestfulKit.addInterceptor(BizInterceptor())
        mRestfulKit.addInterceptor(HttpCodeInterceptor())

    }

    fun <T> create(service: Class<T>): T {
        return mRestfulKit.create(service)
    }
}