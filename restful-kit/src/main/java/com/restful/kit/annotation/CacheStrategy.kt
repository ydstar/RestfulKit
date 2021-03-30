package com.restful.kit.annotation


/**
 * Author: 信仰年轻
 * Date: 2021-01-22 14:50
 * Email: hydznsqk@163.com
 * Des: CacheStrategy 作用在方法和字段上,运行时
 * 例子:
 *  作用在方法上
 *  @CacheStrategy(CacheStrategy.CACHE_FIRST)
 *  @GET("category/categories")
 *  fun queryTabList(): RestfulCall<List<TabCategory>>
 *
 *  作用在参数字段上
 *  @GET("home/{categoryId}")
 *  fun queryTabCategoryList(
 *      @CacheStrategy  CacheStrategy:Int,
 *      @Path("categoryId") categoryId: String,
 *      @Filed("pageIndex") pageIndex: Int,
 *      @Filed("pageSize") pageSize: Int
 *   ): RestfulCall<HomeModel>
 */
@Target(AnnotationTarget.FUNCTION,AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheStrategy(val value: Int = NET_ONLY) {

    companion object {
        const val CACHE_FIRST = 0//请求接口时先读取本地缓存,在请求接口,接口成功后更新缓存(页面初始化数据)
        const val NET_ONLY = 1//仅仅只是请求接口(一般是分页和独立非列表页面)
        const val NET_CACHE = 2//先接口,接口成功后更新缓存(一般是下拉刷新)

    }
}