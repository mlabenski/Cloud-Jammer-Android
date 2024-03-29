package com.geeboff.cloudjammer.api

import com.geeboff.cloudjammer.model.CustomField
import com.geeboff.cloudjammer.model.Product
import com.geeboff.cloudjammer.model.Store
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("/v2/store/products?details=true")
    suspend fun getProductsByStoreId(@Query("storeID") storeID: String): Response<List<Product>>

    @GET("/user/stores")
    suspend fun getStores(@Query("userID") userID: String): Response<List<Store>>

    @GET("/productGroupFieldsHandler")
    suspend fun getProductFields(@Query("store_id") storeId: Int): Response<Map<String, List<CustomField>>>

    @GET("/productGroupFieldsHandler")
    suspend fun getProductGroups(@Query("store_id") storeId: Int, @Query("returnURL") returnURL: Boolean = true): Response<JsonObject>

    @GET("/productGroupDataHandler")
    suspend fun getProductGroupData(@Query("table_name") tableName: String): Response<List<Product>>

    @GET("/productGroupDataHandler")
    suspend fun getProductGroupDataTwo(@Query("table_name") tableName: String): Response<List<Map<String, Any>>>

}