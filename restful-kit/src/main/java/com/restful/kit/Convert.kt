package com.restful.kit

import com.restful.kit.response.RestfulResponse
import java.lang.reflect.Type

/**
 * Author: 信仰年轻
 * Date: 2020-11-05 16:13
 * Email: hydznsqk@163.com
 * Des:
 */
interface Convert {
    fun <T> convert(rawData: String, dataType: Type): RestfulResponse<T>
}