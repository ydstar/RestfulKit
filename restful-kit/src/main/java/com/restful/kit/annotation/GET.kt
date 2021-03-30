package com.restful.kit.annotation

/**
 * Author: 信仰年轻
 * Date: 2020-11-03 14:50
 * Email: hydznsqk@163.com
 * Des: GET 作用在方法,运行时
 * 例子:
 *         @GET("group/{id}/users")
 *         @POST("group/{id}/users",formPost = false)
 *         @DELETE("group/{id}/users",formPost = false)
 *         @PUT("group/{id}/users",formPost = false)
 *         @BaseUrl("https://api.github.com/")
 *         @Headers("auth-token:token", "token")
 *         @CacheStrategy(CacheStrategy.CACHE_FIRST)
 *         fun groupList(@Path("id") groupId: Int,@Filed("page") page: Int): RestfulCall<List<User>>
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GET (val value:String)