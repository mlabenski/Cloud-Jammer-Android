package com.geeboff.cloudjammer.view
import android.os.Bundle
import android.util.Log
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

        // Load custom products
        loadCustomProducts()
    }


    private fun loadCustomProducts() {
        lifecycleScope.launch {
            val customProducts = getCustomProducts()
            // Set up the adapter with the list of custom products
            adapter = CustomProductAdapter(this@CustomProductDisplayActivity, customProducts)
            recyclerView.adapter = adapter
        }
    }

    private suspend fun getCustomProducts(): List<Map<String, Any>> {
        val apiService = RetrofitInstance.retrofit.create(ApiService::class.java)
        return try {
            apiService.getCustomProducts("glassware")
        } catch (e: Exception) {
            Log.e("CustomProductDisplay", "Error fetching custom products", e)
            emptyList()
        }
    }

    interface ApiService {
        @GET("productGroupDataHandler")
        suspend fun getCustomProducts(@Query("table_name") tableName: String): List<Map<String, Any>>
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