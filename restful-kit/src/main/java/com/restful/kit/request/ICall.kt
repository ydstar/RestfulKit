package com.restful.kit.request

import com.restful.kit.response.ICallBack
import com.restful.kit.response.IResponse
import java.io.IOException

/**
 * Author: 信仰年轻
 * Date: 2020-11-03 14:48
 * Email: hydznsqk@163.com
 * Des:
 */
interface ICall<T> {

    /**
     * 发起同步请求
     */
    @Throws(IOException::class)
    fun execute(): IResponse<T>


    /**
     * 发起异步请求
     */
    fun enqueue(callBack: ICallBack<T>)


    /**
     * 发起网络请求的工厂
     */
    interface Factory{
        /**
         * 根据请求信息发起一个新请求
         */
        fun newCall(request: IRequest): ICall<*>
    }
}