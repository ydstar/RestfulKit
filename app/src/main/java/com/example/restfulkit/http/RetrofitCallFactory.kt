package com.example.restfulkit.http


import com.restful.kit.RestfulConvert
import com.restful.kit.request.ICall
import com.restful.kit.request.RestfulRequest
import com.restful.kit.response.ICallBack
import com.restful.kit.response.RestfulResponse
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


    private var mConvert: RestfulConvert
    private var mApiService: ApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .build()
        mApiService = retrofit.create(ApiService::class.java)
        mConvert = GsonConvert()
    }

    override fun newCall(request: RestfulRequest): ICall<Any> {
        return RetrofitCall(request)
    }

    internal inner class RetrofitCall<T>(val request: RestfulRequest) : ICall<T> {
        /**
         * 同步请求
         */
        override fun execute(): RestfulResponse<T> {
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
        private fun createRealCall(request: RestfulRequest): Call<ResponseBody> {
            when (request.httpMethod) {
                RestfulRequest.METHOD.GET -> {
                    return mApiService.get(request.headers,request.endPointUrl(),request.parameters)
                }
                RestfulRequest.METHOD.POST -> {
                    val requestBody: RequestBody = buildRequestBody(request)
                    return mApiService.post(request.headers, request.endPointUrl(), requestBody)
                }
                RestfulRequest.METHOD.PUT -> {
                    val requestBody: RequestBody = buildRequestBody(request)
                    return mApiService.put(request.headers, request.endPointUrl(), requestBody)
                }
                RestfulRequest.METHOD.DELETE -> {
                    return mApiService.delete(request.headers, request.endPointUrl())
                }
                else -> {
                    throw IllegalStateException("RestfulKit只支持 GET,POST,PUT,DELETE请求 ,url=" + request.endPointUrl())
                }
            }
        }

        /**
         * 创建请求body
         */
        private fun buildRequestBody(request: RestfulRequest): RequestBody {
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
         * 解析响应体并封装成RestfulResponse对象
         */
        private fun parseResponse(response: Response<ResponseBody>): RestfulResponse<T> {
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
            return mConvert.convert(rawData!!,request.returnType!!)
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