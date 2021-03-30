package com.restful.kit

import com.restful.kit.request.RestfulCall
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

/**
 * Author: 信仰年轻
 * Date: 2020-11-03 14:49
 * Email: hydznsqk@163.com
 * Des: 仿Retrofit来封装自己的库
 *
 * 思路:
 *  核心思想在于,
 *  1.定义接口然后里面定义要请求的方法,这个方法上有各种注解,不仅支持get,post,path,field,header,还支持baseUrl
 *  2.这个方法的返回值必须是RestfulCall<?>
 *  3.定义create方法,传入接口的class字节码对象得到具接口实现类,然后就调用方法,用了动态代理,当方法被调用的时候就调用了
 *  Proxy的invoke方法,在这个方法内解析请求方法的各种注解然后封装成Request对象,然后把request对象封装成ProxyCall对象
 *  4.这个ProxyCall对象就是RestfulCall<?>的实现类对象,然后在invoke方法内返回,因为动态代理这个invoke要返回的值就是传入的接口中的被调用的方法的返回值
 *   所以必须返回RestfulCall<?>的实现类对象
 *  5.ProxyCall本身是真正的发起网络请求的类,因为ProxyCall对象是用Scheduler的newCall方法创建的,然后方法内是用的callFactory创建的
 *  6.这个callFactory是在外界ApiFactory类中创建RestfulKit的时候传递进来的
 */
class RestfulKit constructor(val baseUrl: String, callFactory: RestfulCall.Factory) {

    //解析的方法map
    private var mMethodParserMap = ConcurrentHashMap<Method, MethodParser>()

    //自定义拦截器list
    private var mInterceptorList = mutableListOf<RestfulInterceptor>()

    private var mScheduler : Scheduler

    /**
     * 添加拦截器
     */
    fun addInterceptor(interceptor: RestfulInterceptor) {
        mInterceptorList.add(interceptor)
    }

    init {
        mScheduler = Scheduler(callFactory,mInterceptorList)
    }

    /**
     * 动态代理
     *
     * interface ApiService {
     *  @GET("group/{id}/users")
     *  @POST("group/{id}/users",formPost = false)
     *  @DELETE("group/{id}/users",formPost = false)
     *  @PUT("group/{id}/users",formPost = false)
     *  @BaseUrl("https://api.github.com/")
     *  @Headers("auth-token:token", "token")
     *  @CacheStrategy(CacheStrategy.CACHE_FIRST)
     *  fun groupList(@Path("id") groupId: Int,@Filed("page") page: Int): RestfulCall<List<User>>
     * }
     */
    fun <T> create(service:Class<T>):T{

        /**
         * 第1个参数:接口的classLoader
         * 第2个参数:接口的class对象
         * 运行的时候就能为我们生成一个代理对象了,可以理解为new了一个Interface对象给了我们
         */
        val t = Proxy.newProxyInstance(service.classLoader,arrayOf<Class<*>>(service),object : InvocationHandler {
            /**
             * 拿ApiService来举例子
             * 当调用了接口中的方法的时候就会回调invoke这个方法,然后会解析该方法上的所有东西(注解,参数,返回值)
             * proxy:接口的代理实现对象,(ApiService的实现类对象)
             * method:调用的方法(groupList方法)
             * args:调用的方法的入参groupList(@Path("id") groupId: Int,@Filed("page") page: Int)
             * 返回值:就是定义接口中方法的返回值RestfulCall<List<User>>
             */
            override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {

                var methodParser = mMethodParserMap[method]
                if (methodParser == null) {
                    //解析函数上的所有注解并封装成RestfulRequest对象
                    methodParser = MethodParser.parse(baseUrl, method)
                    mMethodParserMap[method] = methodParser
                }

                //解析完后得到RestfulRequest对象,还需要封装成RestfulCall接口的实现类对象,
                val request = methodParser.newRequest(method, args)

                //进一步封装成proxyCall,也就是RestfulCall的实现类对象,该类是真正的发起网络请求的类
                val proxyCall = mScheduler.newCall(request)
                return proxyCall
            }
        }) as T
        return  t
    }
}