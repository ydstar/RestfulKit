package com.restful.kit.response

/**
 * Author: 信仰年轻
 * Date: 2020-11-03 14:48
 * Email: hydznsqk@163.com
 * Des:
 */
interface ICallBack<T>{

    /**
     * 请求成功
     */
    fun onSuccess(response: RestfulResponse<T>)


    /**
     * 请求失败
     */
    fun onFailed(throwable: Throwable){}
}