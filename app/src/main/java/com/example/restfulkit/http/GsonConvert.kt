package com.example.restfulkit.http

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.restful.kit.Convert
import com.restful.kit.response.RestfulResponse

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Type

/**
 * Author: 信仰年轻
 * Date: 2020-11-05 16:13
 * Email: hydznsqk@163.com
 * Des: 自定义json解析器
 */
class GsonConvert : Convert {
    private var mGson = Gson()
    override fun <T> convert(rawData: String, dataType: Type): RestfulResponse<T> {
        val response: RestfulResponse<T> = RestfulResponse<T>()
        //解析 rawData 然后赋值给 RestfulResponse
        try {
            val jsonObject = JSONObject(rawData)
            response.code = jsonObject.optInt("code")//code
            response.msg = jsonObject.optString("msg")//msg
            val data = jsonObject.opt("data")//data

            if((data is JSONObject) or (data is JSONArray)){
                if(response.code == RestfulResponse.SUCCESS){
                    response.data = mGson.fromJson(data.toString(), dataType)
                }else{
                    response.errorData = mGson.fromJson<MutableMap<String, String>>(
                        data.toString(),
                        object : TypeToken<MutableMap<String, String>>() {}.type
                    )
                }
            } else {
                response.data = data as? T
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            response.code = -1
            response.msg = e.message
        }
        response.rawData = rawData
        return response
    }
}