package com.example.lkllkllkl.imagesegmentation.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitUtils {

    companion object {
        const val TIME_OUT = 60 * 1000L
        var BASE_URL = "http://192.168.191.7:5000/"

        private val RETROFIT_INSTANCE: Retrofit by lazy {
            val okHttpBuilder = OkHttpClient.Builder()
            okHttpBuilder.connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
            Retrofit.Builder()
                    .client(okHttpBuilder.build())
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }

        fun getRetrofit() = RETROFIT_INSTANCE

        fun <T> getService(clazz: Class<T>) = RETROFIT_INSTANCE.create(clazz)
    }
}