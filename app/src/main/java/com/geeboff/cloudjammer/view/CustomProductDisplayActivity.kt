package com.geeboff.cloudjammer.view
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geeboff.cloudjammer.R
import com.geeboff.cloudjammer.adapter.CustomProductAdapter
import com.geeboff.cloudjammer.model.CustomField
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class CustomProductDisplayActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_product_display)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CustomProductAdapter(this, emptyList())
        recyclerView.adapter = adapter

        //navigation


        // Get the product group name passed from MainActivity
        val productGroupName = intent.getStringExtra("productGroupName")
        val activeItemIndex = intent.getIntExtra("activeItemIndex", -1)

        if (productGroupName != null) {
            loadCustomProducts(productGroupName)
        } else {
            // Handle the error or request the user to select a product group
        }
    }

    private fun loadCustomProducts(productGroup: String) {
        lifecycleScope.launch {
            val customProducts = getCustomProducts(productGroup)
            adapter.updateProducts(customProducts)
        }
    }

    private suspend fun getCustomProducts(productGroup: String): List<Map<String, Any>> {
        val apiService = RetrofitInstance.retrofit.create(ApiService::class.java)
        return try {
            apiService.getCustomProducts(productGroup)
        } catch (e: Exception) {
            Log.e("CustomProductDisplay", "Error fetching custom products for $productGroup", e)
            emptyList()
        }
    }

    private fun showProductGroupSelection(groups: List<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose a Product Group")
        builder.setItems(groups.toTypedArray()) { _, which ->
            val selectedGroup = groups[which]
            loadCustomProducts(selectedGroup)
        }
        builder.show()
    }

    private suspend fun getProductGroups(storeId: Int): List<String> {
        val apiService = RetrofitInstance.retrofit.create(ApiService::class.java)
        return try {
            val response = apiService.getProductFields(storeId)
            response.keys.toList() // Convert the keys of the map to a list of product group names
        } catch (e: Exception) {
            Log.e("CustomProductDisplay", "Error fetching product groups", e)
            emptyList()
        }
    }


    data class Field(
        val name: String,
        val type: String,
        val options: List<String>? = null
    )

    interface ApiService {
        @GET("productGroupDataHandler")
        suspend fun getCustomProducts(@Query("table_name") tableName: String): List<Map<String, Any>>

        @GET("productGroupFieldsHandler")
        suspend fun getProductFields(@Query("store_id") storeId: Int): Map<String, List<Field>>
    }
}

object RetrofitInstance {
    val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val customGson = GsonBuilder()
            .registerTypeAdapter(CustomField::class.java, CustomFieldDeserializer())
            .create()

        Retrofit.Builder()
            .baseUrl("http://192.168.1.183:8080")
            .addConverterFactory(GsonConverterFactory.create(customGson))
            .client(OkHttpClient.Builder().addInterceptor(logging).build())
            .build()
    }
}

class CustomFieldDeserializer : com.google.gson.JsonDeserializer<CustomField> {
    override fun deserialize(json: com.google.gson.JsonElement, typeOfT: java.lang.reflect.Type, context: com.google.gson.JsonDeserializationContext): CustomField {
        json.asJsonObject.apply {
            val name = get("name").asString
            val type = get("type").asString
            val options = if (has("options") && get("options").isJsonArray) {
                get("options").asJsonArray.map { it.asString }
            } else {
                null
            }
            val value = when (type) {
                "INTEGER" -> if (has("value")) get("value").asInt else null
                "TEXT" -> if (has("value")) get("value").asString else null
                // Add other type cases as needed
                else -> null
            }
            return CustomField(name, type, options)
        }
    }
}