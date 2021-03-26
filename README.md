# RestfulKit
简洁但不简单的易用低耦合的网络层框架,提供了网络层操作和网络库的解耦设计,底层提供了可插拔的设计,支持更换底层网络引擎而不影响上层使用

## 前提
因为该框架旨在解耦,所以需要自己在项目中创建网络请求工厂类,和引入三方网络库,具体可以参考demo

## 目前对以下需求进行了封装
* 支持GET,POST,PUT,DELETE请求方式,POST,PUT,DELETE支持表单和application/json
* 支持动态更换接口域名BaseUrl
* 支持动态修改接口相对路由
* 支持添加个性化Header
* 支持传递8种基本类型数据
* 支持拦截器,支持拓展网络引擎实现方式,自定义序列化方式
* 支持3种缓存方式
*   |--- 第一种:请求接口时先读取本地缓存,在请求接口,接口成功后更新缓存
*   |--- 第二种:仅仅只是请求接口
*   |--- 第三种:先接口,接口成功后更新缓存

## 导入方式

仅支持`AndroidX`
```
dependencies {
     implementation 'com.android.ydkit:restful-kit:1.0.0'
}
```

## 简单使用
用法和retrofit保持一致,定义接口

### 定义接口
```java
interface ApiService {
    @GET("group/{id}/users")
    @POST("group/{id}/users",formPost = false)
    @DELETE("group/{id}/users",formPost = false)
    @PUT("group/{id}/users",formPost = false)
    @BaseUrl("https://api.github.com/")
    @Headers("auth-token:token", "token")
    @CacheStrategy(CacheStrategy.CACHE_FIRST)
    fun groupList(@Path("id") groupId: Int,@Filed("page") page: Int): ICall<List<User>>
}

```

### 发起请求

```java
        //初始化
        val baseUrl = "https://api.github.com/"
        val iRestful = IRestful(baseUrl, RetrofitCallFactory(baseUrl))
        iRestful.addInterceptor(BizInterceptor())

        //发起异步请求
        iRestful.create(ApiService::class.java)
            .groupList(1,10)
            .enqueue(object : ICallBack<List<User>> {
                override fun onSuccess(response: IResponse<List<User>>) {
                    val data = response.data
                }

                override fun onFailed(throwable: Throwable) {

                }
            })
```


## 注解说明
| 注解名称      |作用  |
| :-------- | :--------|
| GET       | GET请求方式  |
| POST      | POST请求方式,支持表单与application/json提交 |
| DELETE    | DELETE请求方式,支持表单与application/json提交 |
| PUT       | PUT请求方式,支持表单与application/json提交 |
| Path      | 动态替换GET,POST,DELETE,PUT中的URL占位符 |
| Headers   | 请求头信息 |
| BaseUrl   | 给方法标记上特殊的域名 |
| CacheStrategy   | 缓存方式 |
| CacheStrategy.CACHE_FIRST   | 请求接口时先读取本地缓存,在请求接口,接口成功后更新缓存(页面初始化数据) |
| CacheStrategy.NET_ONLY   | 仅仅只是请求接口(一般是分页和独立非列表页面) |
| CacheStrategy.NET_CACHE   | 先接口,接口成功后更新缓存(一般是下拉刷新) |



## License
```text
Copyright [2021] [ydStar]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```