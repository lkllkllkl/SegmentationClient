package com.example.lkllkllkl.imagesegmentation.data

import com.example.lkllkllkl.imagesegmentation.BuildConfig
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitUtils {

    companion object {
        const val TIME_OUT = 60 * 1000L
        var BASE_URL = "http://192.168.191.7:5000/"

        private val RETROFIT_INSTANCE: Retrofit by lazy {
            val okHttpBuilder = OkHttpClient.Builder()
            val loggingInterceptor = LoggingInterceptor.Builder()
                    .loggable(BuildConfig.DEBUG)
                    .setLevel(Level.BASIC)
                    .log(Platform.INFO)
                    .request("Request")
                    .response("Response")
                    .addHeader("version", BuildConfig.VERSION_NAME)
                    .addQueryParam("query", "0")
                    .build()
            okHttpBuilder.addInterceptor(loggingInterceptor)
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