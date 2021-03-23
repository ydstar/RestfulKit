package com.restful.kit.request

import android.text.TextUtils
import androidx.annotation.IntDef
import com.restful.kit.annotation.CacheStrategy
import java.lang.Exception
import java.lang.StringBuilder
import java.lang.reflect.Type
import java.net.URLEncoder

/**
 * Author: 信仰年轻
 * Date: 2020-11-03 14:49
 * Email: hydznsqk@163.com
 * Des: request请求发起的包装类对象,里面包含了要请求携带的所有信息
 */
open class IRequest {

    //请求方式
    @METHOD
    var httpMethod: Int = 0

    //请求头
    var headers: MutableMap<String, String>? = null

    //请求入参
    var parameters: MutableMap<String, String>? = null

    //请求的域名
    var domainUrl: String? = null

    //相对路径 /cities
    var relativeUrl: String? = null

    //restful方法的泛型返回值
    var returnType: Type? = null

    //是否表单提交,post有效
    var formPost: Boolean = true

    //缓存的key
    private var cacheStrategyKey: String = ""

    //缓存的方式
    var cacheStrategy: Int = CacheStrategy.NET_ONLY

    @IntDef(value = [METHOD.GET, METHOD.POST, METHOD.PUT, METHOD.DELETE])
    annotation class METHOD {
        companion object {
            const val GET = 0
            const val POST = 1
            const val PUT = 2
            const val DELETE = 3
        }
    }


    //返回的是请求的完整的url
    /**
     * //scheme-host-port:443
    //https://www.baidu.com/v1/ ---relativeUrl: user/login===>https://www.baidu.com/v1/user/login
    //可能存在别的域名的场景
    //https://www.baidu.com/v2/
    //https://www.baidu.com/v1/ ---relativeUrl: /v2/user/login===>https://www.baidu.com/v2/user/login
     */
    fun endPointUrl(): String {
        if (relativeUrl == null) {
            throw IllegalStateException("relative url must bot be null ")
        }
        if (!relativeUrl!!.startsWith("/")) {
            return domainUrl + relativeUrl
        }

        val indexOf = domainUrl!!.indexOf("/")
        return domainUrl!!.substring(0, indexOf) + relativeUrl
    }

    fun addHeader(name: String, value: String) {
        if (headers == null) {
            headers = mutableMapOf()
        }
        headers!![name] = value
    }

    /**
     * 获取缓存key,url拼接参数作为 key
     */
     fun getCacheKey(): String {
        if (!TextUtils.isEmpty(cacheStrategyKey)) {
            return cacheStrategyKey
        }
        val sb = StringBuilder()
        val endUrl = endPointUrl()
        sb.append(endUrl)
        if (endUrl.indexOf("?") > 0 || endUrl.indexOf("&") > 0) {
            sb.append("&")
        } else {
            sb.append("?")
        }
        if (parameters != null) {
            for ((key, value) in parameters!!) {
                try {
                    val encodeValue = URLEncoder.encode(value, "UTF-8")
                    sb.append(key).append("=").append(encodeValue).append("&")
                } catch (e: Exception) {

                }
            }
            sb.deleteCharAt(sb.length - 1)
            cacheStrategyKey = sb.toString()
        } else {
            cacheStrategyKey = endUrl
        }

        return cacheStrategyKey
    }

}