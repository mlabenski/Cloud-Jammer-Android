package com.geeboff.cloudjammer.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.geeboff.cloudjammer.api.ApiService
import com.geeboff.cloudjammer.model.CustomField
import com.geeboff.cloudjammer.model.NavProductGroup
import com.google.gson.JsonArray
import com.google.gson.JsonObject

class CustomProductRepository(private val apiService: ApiService) {

    suspend fun getProductGroups(storeId: Int): Result<List<NavProductGroup>> {
        return try {
            val response = apiService.getProductGroups(storeId, true)
            if (response.isSuccessful && response.body() != null) {
                val productGroups = processResponse(response.body()!!)
                Result.success(productGroups)
            } else {
                Result.failure(RuntimeException("Error fetching product groups: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getCustomProducts(tableName: String): List<Map<String, Any>> {
        val response = apiService.getProductGroupDataTwo(tableName)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw RuntimeException("Failed to fetch products: ${response.errorBody()?.string()}")
        }
    }

    private fun processResponse(response: JsonObject): List<NavProductGroup> {
        val productGroups = mutableListOf<NavProductGroup>()

        response.entrySet().forEach { entry ->
            val groupName = entry.key
            val groupObject = entry.value.asJsonObject
            val fieldsJsonArray = groupObject.getAsJsonArray("Fields") ?: JsonArray()
            println("Group name is:")
            println(groupName)
            val fields = fieldsJsonArray.mapNotNull { fieldElement ->
                val fieldObject = fieldElement.asJsonObject
                val fieldName = fieldObject.get("name")?.asString ?: return@mapNotNull null
                val fieldType = fieldObject.get("type")?.asString ?: return@mapNotNull null
                val options = fieldObject.get("options")?.asJsonArray?.mapNotNull { it?.asString }
                CustomField(name = fieldName, type = fieldType, options = options)
            }
            val imageBase64 = groupObject.get("Image")?.asString
            val imageBitmap = imageBase64?.let { base64ToBitmap(it) }

            productGroups.add(NavProductGroup(name = groupName, fields = fields, image = imageBitmap))
        }

        return productGroups
    }

    private fun base64ToBitmap(base64Str: String): Bitmap {
        val imageBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
