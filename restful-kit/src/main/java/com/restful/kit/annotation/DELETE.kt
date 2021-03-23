package com.restful.kit.annotation

/**
 * Author: 信仰年轻
 * Date: 2020-11-03 14:50
 * Email: hydznsqk@163.com
 * Des: DELETE 作用在方法,运行时
 * 例子:
 *      @Headers("auth-token:token", "accountId:123456")
 *      @BaseUrl("https://www.baidu.com/as/")
 *      @POST("/cities/{province}")
 *      @GET("/cities")
 *      @PUT("/cities")
 *      @DELETE("/cities")
 *     fun listCities(@Path("province") province: Int,@Filed("page") page: Int): ICall<JsonObject>
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DELETE (val value:String, val formPost:Boolean=true)