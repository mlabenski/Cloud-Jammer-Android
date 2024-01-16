package com.geeboff.cloudjammer

import android.app.Application
import com.geeboff.cloudjammer.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApplication : Application() {

    lateinit var apiService: ApiService

    override fun onCreate() {
        super.onCreate()
        initializeRetrofit()
    }

    private fun initializeRetrofit() {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.183:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    companion object {
        private lateinit var instance: MyApplication

        fun getApiService(): ApiService {
            return instance.apiService
        }
    }

    init {
        instance = this
    }
}