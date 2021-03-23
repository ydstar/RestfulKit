package com.restful.kit.util

import android.os.Handler
import android.os.Looper
import android.os.Message

/**
 * Author: 信仰年轻
 * Date: 2020-11-27 17:28
 * Email: hydznsqk@163.com
 * Des:
 */
object MainHandler {
    private val handler = Handler(Looper.getMainLooper())

    fun post(runnable: Runnable) {
        handler.post(runnable)
    }

    fun postDelay(delayMills: Long, runnable: Runnable) {
        handler.postDelayed(runnable, delayMills)
    }

    fun sendAtFrontOfQueue(runnable: Runnable) {
        val msg = Message.obtain(handler, runnable)
        handler.sendMessageAtFrontOfQueue(msg)
    }

    fun remove(runnable: Runnable) {
        handler.removeCallbacks(runnable)
    }
}