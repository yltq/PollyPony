package com.bubble.pollypony.web

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface PonyWebFace {
    @GET("/")
    fun webShort(@Header("User-Agent") agent: String): Call<ResponseBody>
}