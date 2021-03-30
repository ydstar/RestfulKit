package com.restful.kit.annotation

/**
 * Author: 信仰年轻
 * Date: 2020-11-03 14:50
 * Email: hydznsqk@163.com
 * Des: Headers 作用在方法上,运行时
 * 例子:
 *         @GET("group/{id}/users")
 *         @POST("group/{id}/users",formPost = false)
 *         @DELETE("group/{id}/users",formPost = false)
 *         @PUT("group/{id}/users",formPost = false)
 *         @BaseUrl("https://api.github.com/")
 *         @Headers("auth-token:token", "token")
 *         @CacheStrategy(CacheStrategy.CACHE_FIRST)
 *         fun groupList(@Path("id") groupId: Int,@Filed("page") page: Int): ICall<List<User>>
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Headers (vararg val value:String)//headers 有可能会有多个,所以用vararg可变参数修饰