package com.geeboff.cloudjammer.api

import com.geeboff.cloudjammer.model.Product
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/v2/store/products?details=true")
    fun getProductsByStoreId(@Query("storeID") storeID: String): Call<List<Product>>
}