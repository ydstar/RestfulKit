package com.example.restfulkit.api


import com.google.gson.JsonObject
import com.restful.kit.annotation.*
import com.restful.kit.request.ICall


interface ApiService {

    @GET("group/{id}/users")
    @POST("group/{id}/users",formPost = false)
    @DELETE("group/{id}/users",formPost = false)
    @PUT("group/{id}/users",formPost = false)
    @BaseUrl("https://api.github.com/")
    @Headers("auth-token:token", "token")
    @CacheStrategy(CacheStrategy.CACHE_FIRST)
    fun groupList(@Path("id") groupId: Int,@Filed("page") page: Int): ICall<List<User>>


    @POST("user/login")
    fun login(
        @Filed("userName") userName: String,
        @Filed("password") password: String
    ): ICall<String>
}

