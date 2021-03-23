package com.restful.kit.util

import android.os.Handler
import android.os.Looper
import androidx.annotation.IntRange

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

/**
 * Author: 信仰年轻
 * Date: 2020-11-12 16:23
 * Email: hydznsqk@163.com
 * Des:管理线程
 * 支持按任务的优先级去执行
 * 支持线程池暂停.恢复(批量文件下载，上传)
 * 异步结果主动回调主线程
 */
object IExecutor {
    private const val TAG: String = "IExecutor"

    private var iExecutor: ThreadPoolExecutor
    private var mHandler:Handler = Handler(Looper.getMainLooper())

    private var mIsPause = false
    private val mReentrantLock:ReentrantLock = ReentrantLock() //可重入锁
    private var mNewCondition: Condition

    init {
        mNewCondition = mReentrantLock.newCondition()

        val cpuCount = Runtime.getRuntime().availableProcessors()
        val corePooSize = cpuCount + 1
        val maxPoolSize = cpuCount * 2 + 1
        val blockingQueue: PriorityBlockingQueue<out Runnable> = PriorityBlockingQueue()
        val keepAliveTime = 30L
        val unit = TimeUnit.SECONDS

        val atomicLong = AtomicLong()
        val threadFactory = ThreadFactory {
            val thread = Thread(it)
            thread.name = "i-executor-" + atomicLong.getAndIncrement()
            return@ThreadFactory thread
        }

        iExecutor = object : ThreadPoolExecutor(corePooSize, maxPoolSize, keepAliveTime,
            unit, blockingQueue as BlockingQueue<Runnable>, threadFactory) {
            /**
             * 线程执行前
             */
            override fun beforeExecute(t: Thread?, r: Runnable?) {
                if(mIsPause){
                    mReentrantLock.lock()
                    try {
                        //如果暂停了就让线程等待
                        mNewCondition.await()
                    }finally {
                        mReentrantLock.unlock()
                    }
                }
            }

            /**
             * 线程执行后
             */
            override fun afterExecute(r: Runnable?, t: Throwable?) {
                //监控线程池耗时任务,线程创建数量,正在运行的数量
            }
        }
    }


    @JvmOverloads
    fun execute(@IntRange(from = 0,to = 10) priority:Int=0,runnable: Runnable){
        iExecutor.execute(PriorityRunnable(priority,runnable))
    }

    @JvmOverloads
    fun execute(@IntRange(from = 0, to = 10) priority: Int = 0, runnable: Callable<*>) {
        iExecutor.execute(PriorityRunnable(priority, runnable))
    }


    /**
     * 暂停线程
     */
    @Synchronized
    fun pause() {
        mIsPause = true
    }

    /**
     * 恢复线程
     */
    fun resume() {
        mIsPause = false
        mReentrantLock.lock()
        try {
            //唤醒所有阻塞的线程
            mNewCondition.signalAll()
        } finally {
            mReentrantLock.unlock()
        }
    }

    /**
     * 对Runnable对象的包装类,是具有可比较线程优先级的Runnable
     */
    class PriorityRunnable(val priority:Int, private val runnable: Runnable)
        :Runnable,Comparable<PriorityRunnable>{

        override fun run() {
            runnable.run()
        }

        override fun compareTo(other: PriorityRunnable): Int {
            if(this.priority<other.priority){
                return 1
            }else if (this.priority>other.priority){
                return -1
            }else{
                return 0
            }
        }
    }

    abstract class Callable<T>:Runnable{
        override fun run() {
            mHandler.post{
                onPrepare()
            }

            val t = onBackground()
            //移除所有消息.防止需要执行onCompleted了，onPrepare还没被执行，那就不需要执行了
            mHandler.removeCallbacksAndMessages(null)
            mHandler.post{
                onCompleted(t)
            }
        }
        //任务执行前
        open fun onPrepare(){
            //可以转菊花
        }

        //真正后台任务的地方
        abstract fun onBackground(): T
        //任务执行完
        abstract fun onCompleted(t: T)
    }
}