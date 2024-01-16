package com.geeboff.cloudjammer.repository

import com.geeboff.cloudjammer.api.ApiService
import com.geeboff.cloudjammer.model.Product

class ProductRepository(private val apiService: ApiService) {

    suspend fun getProductsByStoreId(storeId: String): Result<List<Product>> {
        return try {
            val response = apiService.getProductsByStoreId(storeId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Handle errors
                Result.failure(RuntimeException("Error fetching products: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            // Handle exceptions such as network errors
            Result.failure(e)
        }
    }
}
