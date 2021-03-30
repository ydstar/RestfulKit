package com.example.restfulkit.http

import android.util.Log

import com.restful.kit.RestfulInterceptor
import com.restful.kit.request.RestfulRequest


/**
 * Author: 信仰年轻
 * Date: 2020-11-05 17:02
 * Email: hydznsqk@163.com
 * Des: 业务拦截器,在请求中添加header和拦截返回并打印log
 */
class BizInterceptor: RestfulInterceptor {

    override fun intercept(chain: RestfulInterceptor.Chain): Boolean {
        val request = chain.request()
        val response = chain.response()
        if (chain.isRequestPeriod) {
            //添加token
            request.addHeader("auth-token", "token")
        } else if (response != null) {
            val outputBuilder = StringBuilder()
            val httpMethod: String =
                if (request.httpMethod == RestfulRequest.METHOD.GET) "GET" else "POST"
            val requestUrl: String = request.endPointUrl()
            outputBuilder.append("\n$requestUrl==>$httpMethod\n")


            if (request.headers != null) {
                outputBuilder.append("【headers\n")
                request.headers!!.forEach(action = {
                    outputBuilder.append(it.key + ":" + it.value)
                    outputBuilder.append("\n")
                })
                outputBuilder.append("headers】\n")
            }

            if (request.parameters != null && request.parameters!!.isNotEmpty()) {
                outputBuilder.append("【parameters==>\n")
                request.parameters!!.forEach(action = {
                    outputBuilder.append(it.key + ":" + it.value + "\n")
                })
                outputBuilder.append("parameters】\n")
            }

            outputBuilder.append("【response==>\n")
            outputBuilder.append(response.rawData + "\n")
            outputBuilder.append("response】\n")

            Log.d("BizInterceptor Http", outputBuilder.toString())
        }

        return false
    }
}