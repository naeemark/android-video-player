package com.andromeda.kunalbhatia.demo.hungamaplayer.rest

import android.util.Log
import com.andromeda.kunalbhatia.demo.hungamaplayer.VideoPlayerRecyclerView
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {
    companion object
    {
        var volumeValue: VideoPlayerRecyclerView.VolumeState = VideoPlayerRecyclerView.VolumeState.OFF
        var myToken = ""
        private var client: OkHttpClient? = null
        val API_VERSION = "v3.0/"
        val BASE_URL = "https://dev.ballogy.com/api/$API_VERSION"
        var retrofit: Retrofit? = null

        fun getClient(): Retrofit? {

            buildClient()

            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
            }
            return retrofit
        }

        private fun buildClient() {

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val response: Response
                    Log.e("sendToken", "true>>>>>>>>>>>>>>>>>>>>>>>>>>>")
                    val request: Request
                    val token = myToken
                    request = if (token != null && !token.isEmpty()) {
                        Log.d("TAG", "intercept: AFTER AUTHENTICATION")
                        original.newBuilder()
                            .header("Authorization", "Token $token") // Android
                            .method(original.method, original.body)
                            .build()
                    } else {
                        Log.d("TAG", "intercept: BEFORE AUTHENTICATION")
                        original.newBuilder().method(original.method, original.body)
                            .build()
                    }
                    response = chain.proceed(request)
                    response
                }
                .connectTimeout(80, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .readTimeout(80, TimeUnit.SECONDS)
                .build()
        }
    }
}