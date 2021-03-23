package com.restful.kit.annotation

/**
 * Author: 信仰年轻
 * Date: 2020-11-03 14:50
 * Email: hydznsqk@163.com
 * Des: Path 作用在参数上,运行时
 * 例子:
 *      @Headers("auth-token:token", "accountId:123456")
 *      @BaseUrl("https://www.baidu.com/as/")
 *      @POST("/cities/{province}")
 *      @GET("/cities")
 *     fun listCities(@Path("province") province: Int,@Filed("page") page: Int): ICall<JsonObject>
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Path (val value:String)