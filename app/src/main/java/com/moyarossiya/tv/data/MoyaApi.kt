package com.moyarossiya.tv.data

import com.moyarossiya.tv.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import java.util.concurrent.TimeUnit

interface MoyaApi {
    @Headers("Cache-Control: no-cache")
    @GET("now.json")
    suspend fun now(): NowResponse

    @Headers("Cache-Control: no-cache")
    @GET("next.json")
    suspend fun next(): NextResponse

    @Headers("Cache-Control: no-cache")
    @GET("plan.json")
    suspend fun schedule(): ScheduleResponse

    companion object {
        fun create(): MoyaApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            return Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(MoyaApi::class.java)
        }
    }
}
