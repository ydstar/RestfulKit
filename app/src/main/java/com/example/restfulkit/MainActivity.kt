package com.example.restfulkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.restfulkit.http.ApiFactory
import com.example.restfulkit.api.HomeApi

import com.restful.kit.response.ICallBack
import com.restful.kit.response.IResponse


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * POST请求
     */
    fun onClickPrint(view: View?) {
        ApiFactory
            .create(HomeApi::class.java)
            .login("swifty", "123456")
            .enqueue(object : ICallBack<String> {
                override fun onSuccess(response: IResponse<String>) {
                    if (response.code == IResponse.SUCCESS) {
                        println(response.data!! + "  SUCCESS 线程: "+Thread.currentThread().name)
                    } else {
                        println(response.msg!!+ "  fail 线程: "+Thread.currentThread().name)
                    }
                    showToast(response.msg!!)
                }

                override fun onFailed(throwable: Throwable) {
                    println(throwable.message!!+ "   线程: "+Thread.currentThread().name)
                    showToast(throwable.message)
                }
            })
    }

    fun showToast(message: String?) {
        if (TextUtils.isEmpty(message)) return
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}