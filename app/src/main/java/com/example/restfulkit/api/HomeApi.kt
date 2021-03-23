package com.example.restfulkit.api


import com.restful.kit.annotation.Filed
import com.restful.kit.annotation.POST
import com.restful.kit.request.ICall


interface HomeApi {

    @POST("user/login")
    fun login(
        @Filed("userName") userName: String,
        @Filed("password") password: String
    ): ICall<String>

}