package com.restful.kit

import com.restful.kit.request.RestfulRequest
import com.restful.kit.response.RestfulResponse

/**
 * Author: 信仰年轻
 * Date: 2020-11-03 14:49
 * Email: hydznsqk@163.com
 * Des: 拦截器的顶层接口,发起请求和收到响应都会执行拦截器
 */
interface RestfulInterceptor {

    /**
     * 是否拦截
     */
    fun intercept(chain: Chain):Boolean

    /**
     * Chain 对象会在我们派发拦截器的时候 创建
     */
    interface Chain{

        //当前是否是request阶段
        val isRequestPeriod:Boolean get()=false

        fun request(): RestfulRequest

        /**
         * 这个response对象 在网络发起之前 ，是为空的
         */
        fun response(): RestfulResponse<*>?
    }
}