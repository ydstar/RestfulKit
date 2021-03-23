package com.example.restfulkit.http

import com.restful.kit.IInterceptor


/**
 * Author: 信仰年轻
 * Date: 2020-11-20 12:02
 * Email: hydznsqk@163.com
 * Des:根据response 的 code 自动路由到相关页面
 */
class HttpCodeInterceptor: IInterceptor {

    companion object{
        const val RC_NEED_LOGIN = 5003               //请先登录
    }

    override fun intercept(chain: IInterceptor.Chain): Boolean {

        val response = chain.response()
        if(!chain.isRequestPeriod && response!=null){
            when (response.code) {
                //重新登录
                RC_NEED_LOGIN -> {
                    //todo 跳转到登录页面
                }
            }
        }
        return false
    }
}