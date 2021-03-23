package com.restful.kit


import com.restful.kit.cache.IStorage
import com.restful.kit.annotation.CacheStrategy
import com.restful.kit.request.ICall
import com.restful.kit.request.IRequest
import com.restful.kit.response.ICallBack
import com.restful.kit.response.IResponse
import com.restful.kit.util.IExecutor
import com.restful.kit.util.MainHandler


/**
 * Author: 信仰年轻
 * Date: 2020-11-04 17:28
 * Email: hydznsqk@163.com
 * Des:代理CallFactory创建出来的call对象，从而实现拦截器的派发动作
 */
class Scheduler(
    private val callFactory: ICall.Factory,
    private val interceptorList: MutableList<IInterceptor>
) {


    fun newCall(request: IRequest): ICall<*> {
        val iCall = callFactory.newCall(request)
        return ProxyCall(iCall, request)
    }

    internal inner class ProxyCall<T> : ICall<T> {

        private val mDelegate: ICall<T>
        private val mRequest: IRequest

        constructor(delegate: ICall<T>, request: IRequest) {
            this.mDelegate = delegate
            this.mRequest = request
        }

        /**
         * 同步请求
         */
        override fun execute(): IResponse<T> {
            //请求前的拦截器的派发
            dispatchInterceptor(mRequest, null)

            //读取缓存并进行返回
            if(mRequest.cacheStrategy== CacheStrategy.CACHE_FIRST){
                val cacheResponse = readCache<T>()
                if(cacheResponse.data!=null){
                    return cacheResponse
                }
            }

            //真正的请求
            val response = mDelegate.execute()
            //保存缓存到本地
            saveCacheIfNeed(response)
            //请求后的拦截器的派发
            dispatchInterceptor(mRequest, response)
            return response
        }

        /**
         * 异步请求
         */
        override fun enqueue(callBack: ICallBack<T>) {
            //请求前的拦截器的派发
            dispatchInterceptor(mRequest, null)
            if (mRequest.cacheStrategy == CacheStrategy.CACHE_FIRST) {
                //开子线程去从数据库中拿缓存的数据,拿到后切到主线程把数据返出去
                IExecutor.execute(runnable = Runnable {
                    val cacheResponse = readCache<T>()
                    if (cacheResponse.data != null) {
                        //切换到主线程
                        MainHandler.sendAtFrontOfQueue(runnable = Runnable {
                            callBack.onSuccess(cacheResponse)
                        })
                    }
                })
            }

            //真正的请求
            mDelegate.enqueue(object : ICallBack<T> {
                override fun onSuccess(response: IResponse<T>) {
                    //请求后的拦截器的派发
                    dispatchInterceptor(mRequest, response)
                    saveCacheIfNeed(response)
                    callBack.onSuccess(response)
                }

                override fun onFailed(throwable: Throwable) {
                    callBack.onFailed(throwable)
                }
            })
        }

        /**
         * 保存缓存到本地如果需要的话(CacheStrategy.CACHE_FIRST 或 CacheStrategy.NET_CACHE)
         */
        private fun saveCacheIfNeed(response: IResponse<T>) {
            if (mRequest.cacheStrategy == CacheStrategy.CACHE_FIRST ||
                mRequest.cacheStrategy == CacheStrategy.NET_CACHE) {

                if(response.data!=null){
                    IExecutor.execute(runnable = Runnable {
                        IStorage.saveCache(mRequest.getCacheKey(),response.data)
                    })
                }
            }
        }

        /**
         * 读取缓存并包装成IResponse对象
         */
        private fun <T> readCache(): IResponse<T> {
            //IStorage查找缓存的时候,需要提供一个cache key
            val cacheKey = mRequest.getCacheKey()
            val cache = IStorage.getCache<T>(cacheKey)
            val cacheResponse = IResponse<T>()
            cacheResponse.data = cache
            cacheResponse.code = IResponse.CACHE_SUCCESS
            cacheResponse.msg = "缓存获取成功"
            return cacheResponse
        }


        /**
         * 派发拦截器
         */
        private fun dispatchInterceptor(request: IRequest, response: IResponse<T>?) {
            if (interceptorList.size <= 0) {
                return
            }
            //创建拦截器对象
            val interceptorChain = InterceptorChain(request, response)
            //开始派发
            interceptorChain.dispatch()
        }


        /**
         * 实现拦截器的接口
         */
        internal inner class InterceptorChain(var request: IRequest, var response: IResponse<T>?) :
            IInterceptor.Chain {

            //当前是否是request阶段,response为空就是在请求阶段
            override val isRequestPeriod: Boolean
                get() = response == null

            override fun request(): IRequest {
                return request
            }

            override fun response(): IResponse<*>? {
                return response
            }

            //代表的是 分发的第几个拦截器
            var callIndex: Int = 0

            fun dispatch() {
                val iInterceptor = interceptorList[callIndex]
                val intercept = iInterceptor.intercept(this)
                callIndex++
                //如果不拦截 && 当前的索引 < 拦截器的总数量
                if (!intercept && callIndex < interceptorList.size) {
                    dispatch()
                }
            }
        }
    }


}