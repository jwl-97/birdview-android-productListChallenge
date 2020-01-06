package com.jiwoolee.productlistchallenge.retrofit

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private var instance: Retrofit? = null
    fun getInstance(): Retrofit? {
        if (instance == null) {
            instance = Retrofit.Builder()
                .baseUrl("https://6uqljnm1pb.execute-api.ap-northeast-2.amazonaws.com/prod/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }
        return instance
    }
}
