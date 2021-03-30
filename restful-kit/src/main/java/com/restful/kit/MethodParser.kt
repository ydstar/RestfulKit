package com.restful.kit


import com.restful.kit.request.ICall
import com.restful.kit.request.RestfulRequest
import com.restful.kit.annotation.*
import java.lang.IllegalStateException
import java.lang.reflect.*

/**
 * Author: 信仰年轻
 * Date: 2020-11-03 14:50
 * Email: hydznsqk@163.com
 * Des: 方法解析器,解析注解,解析入参,解析返回值
 */
class MethodParser(private val baseUrl: String, method: Method) {

    //请求方式
    @RestfulRequest.METHOD
    var httpMethod: Int = 0

    //请求头
    var headersMap: MutableMap<String, String> = mutableMapOf()

    //请求入参
    var parametersMap: MutableMap<String, String> = mutableMapOf()

    //请求的域名
    var domainUrl: String? = null

    //相对路径 /cities
    var relativeUrl: String? = null

    private var replaceRelativeUrl: String? = null

    //restful方法的泛型返回值
    var returnType: Type? = null

    //是否表单提交,post有效
    var formPost: Boolean = true

    //缓存的方式
    private var cacheStrategy:Int = CacheStrategy.NET_ONLY


    companion object {
        fun parse(baseUrl: String, method: Method): MethodParser {
            return MethodParser(baseUrl, method)
        }
    }

    /**
     * 解析方法上的注解,解析返回值类型,还有解析入参
     * 但是因为参数是会改变的,所以解析入参放在了每次newRequest方法中
     */
    init {
        //解析方法上的注解get,headers,post baseUrl
        parseMethodAnnotations(method)
        //解析返回值类型
        parseMethodReturnType(method)
    }

    /**
     * 解析方法上的注解
     *  @GET("group/{id}/users")
     *  @POST("group/{id}/users",formPost = false)
     *  @DELETE("group/{id}/users",formPost = false)
     *  @PUT("group/{id}/users",formPost = false)
     *  @BaseUrl("https://api.github.com/")
     *  @Headers("auth-token:token", "token")
     *  @CacheStrategy(CacheStrategy.CACHE_FIRST)
     *  fun groupList(@Path("id") groupId: Int,@Filed("page") page: Int): ICall<List<User>>
     */
    private fun parseMethodAnnotations(method: Method) {
        val annotations = method.annotations
        for (annotation in annotations) {
            if (annotation is GET) {
                relativeUrl = annotation.value
                httpMethod = RestfulRequest.METHOD.GET
            } else if (annotation is POST) {
                relativeUrl = annotation.value
                httpMethod = RestfulRequest.METHOD.POST
                formPost = annotation.formPost
            } else if (annotation is PUT) {
                formPost = annotation.formPost
                httpMethod = RestfulRequest.METHOD.PUT
                relativeUrl = annotation.value
            } else if (annotation is DELETE) {
                httpMethod = RestfulRequest.METHOD.DELETE
                relativeUrl = annotation.value
            } else if (annotation is Headers) {
                val headersList = annotation.value
                //@Headers("auth-token:token", "accountId:123456")
                if (headersList.isNotEmpty()) {
                    for (header in headersList) {
                        val index = header.indexOf(":")
                        //如果这个冒号写在开头或者说没有冒号
                        check(!(index == 0 || index == -1)) {
                            String.format("@headers 注解必须是 [name:value] ,but found [%s]",header)
                        }
                        val name = header.substring(0, index)
                        val value = header.substring(index + 1).trim()
                        headersMap[name] = value
                    }
                }
            } else if (annotation is BaseUrl) {
                domainUrl = annotation.value
            } else if (annotation is CacheStrategy) {
                cacheStrategy = annotation.value
            }  else {
                throw IllegalStateException("无法处理方法注解:" + annotation.javaClass.toString())
            }
        }

        //require是检查,如果 httpMethod 既不是GET,也不是POST,也不是PUT,也不是DELETE,就会抛出异常
        require(
            (httpMethod == RestfulRequest.METHOD.GET)
                    || (httpMethod == RestfulRequest.METHOD.POST
                    || (httpMethod == RestfulRequest.METHOD.PUT)
                    || (httpMethod == RestfulRequest.METHOD.DELETE))
        ) {
            String.format("方法 %s 必须具有GET、POST、PUT,DELETE注解之一", method.name)
        }
        if (domainUrl == null) {
            domainUrl = baseUrl
        }
    }

    /**
     * 解析返回值类型
     * 1.判断返回值是不是ICall,如果不是就抛出异常
     * 2.判断泛型里面的参数只能有1个,必须只能有1个
     * 3.检查泛型内的参数不能是接口,不能是未知类型的
     */
    private fun parseMethodReturnType(method: Method) {
        //如果返回值类型
        if (method.returnType != ICall::class.java) {
            val format = String.format("方法 %s 的返回值必须是ICall.class", method.name)
            throw IllegalStateException(format)//非法状态异常
        }

        val genericReturnType = method.genericReturnType
        //如果返回值的泛型是有泛型参数的
        if (genericReturnType is ParameterizedType) {

            val actualTypeArguments = genericReturnType.actualTypeArguments
            //如果泛型内的参数不是1个就要抛出异常
            require(actualTypeArguments.size == 1) {
                "方法 %s 只能有一个泛型返回类型"
            }

            val argument = actualTypeArguments[0]
            require(validateGenericType(argument)) {
                String.format("方法 %s 泛型返回类型不能是未知类型" + method.name)
            }
            returnType = argument
        } else {
            val format = String.format("方法 %s 必须有一个泛型返回类型", method.name)
            throw IllegalStateException(format)
        }
    }

    /**
     * 验证泛型类型
     */
    private fun validateGenericType(type: Type): Boolean {
        /**
         *wrong 错误
         *  fun test():ICall<Any>
         *  fun test():ICall<List<*>>
         *  fun test():ICall<ApiInterface>
         *expect 正确的预期
         *  fun test():ICall<User>
         */
        //如果指定的泛型是集合类型的，那还检查集合的泛型参数
        if (type is GenericArrayType) {
            return validateGenericType(type.genericComponentType)
        }
        //如果指定的泛型是一个接口 也不行
        if (type is TypeVariable<*>) {
            return false
        }
        //如果指定的泛型是一个通配符 ？extends Number 也不行
        if (type is WildcardType) {
            return false
        }
        return true
    }

    /**
     * 根据method和args参数以及各种注解什么的封装成RestfulRequest对象
     */
    fun newRequest(method: Method, args: Array<out Any>?): RestfulRequest {
        val arguments: Array<Any> = args as Array<Any>? ?: arrayOf()
        //解析入参
        parseMethodParameters(method,arguments)
        val request = RestfulRequest()
        request.domainUrl = domainUrl
        request.returnType = returnType
        request.relativeUrl = replaceRelativeUrl ?: relativeUrl
        request.parameters = parametersMap
        request.headers = headersMap
        request.httpMethod = httpMethod
        request.formPost = formPost
        request.cacheStrategy = cacheStrategy
        return request
    }


    /**
     * 解析参数上的入参
     *  POST("/cities/{province}")
     *  fun listCities(@Path("province") province: Int,@Filed("page") page: Int): ICall<JsonObject>
     * 1.判断入参上都加注解了没,必须都加注解,目前支持Path或Field
     * 2.遍历传入的所有参数,看单个参数上的注解数量是否是1个,如果不是则抛出异常
     * 3.单个参数必须是基本数据类型和String类型,否则抛出异常
     * 4.判断注解,如果是Field,拿到注解上的名称值(比如page),然后拿到传入的参数的值(比如1),存到parametersMap中
     * 5.判断注解,如果是Path,拿到注解的名称值(比如province),拿到传入参数的值(比如bj)
     * 6.对relativeUrl进行替换 把 /cities/{province}  替换成  /cities/bj
     * 7.然后赋值给replaceRelativeUrl
     */
    private fun parseMethodParameters(method: Method, args: Array<Any>) {
        parametersMap.clear()

        //方法上的所有入参的注解数组,二维数组,因为一个入参上可能有多个注解
        val parameterAnnotations = method.parameterAnnotations
        //入参都要标记注解,如果没有标记注解,这传进来的args参数和注解上的parameterAnnotations参数个数就不一致,就会抛出异常
        val equals = parameterAnnotations.size == args.size

        require(equals){
            String.format("参数注解计数 %s 与预期计数不匹配 %s",parameterAnnotations.size,args.size)
        }

        //遍历所有真实入参
        for(index in args.indices){
            //单个参数上的注解list
            val annotationList = parameterAnnotations[index]
            //如果annotationList注解的个数 > 1就会抛出异常
            require(annotationList.size<=1){//不满足条件才会进入
                "字段只能有一个注释：索引=$index"
            }
            //参数
            val argValue = args[index]
            require(isPrimitive(argValue)){//不满足条件才会进入
                "目前支持8种基本类型和String类型，索引=$index"
            }

            val annotation = annotationList[0]
            if(annotation is Filed){
                val value = annotation.value
                parametersMap[value] = argValue.toString()
            }else if(annotation is Path){
                val replaceName = annotation.value
                val replacement = argValue.toString()
                if (replaceName != null && replaceName != null) {
                    replaceRelativeUrl = relativeUrl!!.replace("{$replaceName}", replacement)
                }
            }else if(annotation is CacheStrategy){
                cacheStrategy = argValue as Int
            }else{
                throw IllegalStateException("无法处理参数注解:" + annotation.javaClass.toString())
            }
        }
    }

    /**
     * 是否是8种基本数据类型和String类型
     */
    private fun isPrimitive(value:Any):Boolean{
        //String
        if(value.javaClass == String::class.java){
            return true
        }
        try {
            //8种基本数据类型 int byte short long boolean char double float
            val field = value.javaClass.getField("TYPE")
            val clazz = field[null] as Class<*>
            if(clazz.isPrimitive){
                return  true
            }
        }catch (e:IllegalAccessException){
            e.printStackTrace()
        }catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
        return false
    }


}