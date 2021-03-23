package com.example.restfulkit.http

import com.restful.kit.IConvert
import com.restful.kit.request.ICall
import com.restful.kit.request.IRequest
import com.restful.kit.response.ICallBack
import com.restful.kit.response.IResponse
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*
import java.lang.IllegalStateException

/**
 * Author: 信仰年轻
 * Date: 2020-11-05 15:52
 * Email: hydznsqk@163.com
 * Des:自定义网络请求工厂类
 */
class RetrofitCallFactory(baseUrl: String) : ICall.Factory {


    private var iConvert: IConvert
    private var apiService: ApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .build()
        apiService = retrofit.create(ApiService::class.java)
        iConvert = GsonConvert()
    }

    override fun newCall(request: IRequest): ICall<Any> {
        return RetrofitCall(request)
    }

    internal inner class RetrofitCall<T>(val request: IRequest) : ICall<T> {
        /**
         * 同步请求
         */
        override fun execute(): IResponse<T> {
            //创建真正的请求call
            val realCall: Call<ResponseBody> = createRealCall(request)
            //真正的同步请求
            val response: Response<ResponseBody> = realCall.execute()
            //解析response并进行返回
            return parseResponse(response)
        }

        /**
         * 异步请求
         */
        override fun enqueue(callBack: ICallBack<T>) {
            //创建真正的请求call
            val realCall: Call<ResponseBody> = createRealCall(request)
            realCall.enqueue(object : Callback<ResponseBody>{
                override fun onResponse(call: Call<ResponseBody>,response: Response<ResponseBody>) {
                    val parseResponse = parseResponse(response)
                    callBack.onSuccess(parseResponse)
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    callBack.onFailed(t)
                }
            })
        }

        /**
         * 创建真正的请求call,就是retrofit的发起请求
         */
        private fun createRealCall(request: IRequest): Call<ResponseBody> {
            when (request.httpMethod) {
                IRequest.METHOD.GET -> {
                    return apiService.get(request.headers,request.endPointUrl(),request.parameters)
                }
                IRequest.METHOD.POST -> {
                    val requestBody: RequestBody = buildRequestBody(request)
                    return apiService.post(request.headers, request.endPointUrl(), requestBody)
                }
                IRequest.METHOD.PUT -> {
                    val requestBody: RequestBody = buildRequestBody(request)
                    return apiService.put(request.headers, request.endPointUrl(), requestBody)
                }
                IRequest.METHOD.DELETE -> {
                    return apiService.delete(request.headers, request.endPointUrl())
                }
                else -> {
                    throw IllegalStateException("iRestful只支持 GET,POST,PUT,DELETE请求 ,url=" + request.endPointUrl())
                }
            }
        }

        /**
         * 创建请求body
         */
        private fun buildRequestBody(request: IRequest): RequestBody {
            val parameters: MutableMap<String, String>? = request.parameters

            val builder = FormBody.Builder()
            val requestBody: RequestBody
            val jsonObject = JSONObject()

            if (parameters != null) {
                for ((key, value) in parameters) {
                    if (request.formPost) {
                        builder.add(key, value)
                    } else {
                        jsonObject.put(key, value)
                    }
                }
            }
            if (request.formPost) {
                requestBody = builder.build()
            } else {
                //bugfix: reuqest-header   content-type="application/json; charset=utf-8"
                requestBody = RequestBody.create(
                    MediaType.parse("application/json;charset=utf-8"),
                    jsonObject.toString()
                )
            }
            return requestBody
        }


        /**
         * 解析响应体并封装成IResponse对象
         */
        private fun parseResponse(response: Response<ResponseBody>): IResponse<T> {
          var rawData:String? = null
            //请求成功
            if(response.isSuccessful){
                val body:ResponseBody? = response.body()
                if(body!=null){
                    rawData=body.string()
                }
            }else{
                val errorBody = response.errorBody()
                if (errorBody != null) {
                    rawData = errorBody.string()
                }
            }
            return iConvert.convert(rawData!!,request.returnType!!)
        }
    }


    interface ApiService {

        @GET
        fun get(
            @HeaderMap headers: MutableMap<String, String>?, @Url url: String,
            @QueryMap(encoded = true) params: MutableMap<String, String>?
        ): Call<ResponseBody>

        @POST
        fun post(
            @HeaderMap headers: MutableMap<String, String>?, @Url url: String,
            @Body body: RequestBody?
        ): Call<ResponseBody>

        @PUT
        fun put(
            @HeaderMap headers: MutableMap<String, String>?,
            @Url url: String,
            @Body body: RequestBody?
        ): Call<ResponseBody>

        @DELETE//不可以携带requestbody
        fun delete(
            @HeaderMap headers: MutableMap<String, String>?,
            @Url url: String
        ): Call<ResponseBody>
    }
}