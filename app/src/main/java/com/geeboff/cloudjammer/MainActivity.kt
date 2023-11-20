package com.geeboff.cloudjammer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geeboff.cloudjammer.adapter.ProductAdapter
import com.geeboff.cloudjammer.api.ApiService
import com.geeboff.cloudjammer.model.Product
import com.geeboff.cloudjammer.model.ProductItem
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppTheme)
        setContentView(R.layout.activity_main)

        // set up the tool bar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Remove title text
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // set up the settings icon click listener
        val settingsIcon: ImageView = findViewById(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            // Start the SettingsActivity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        recyclerView = findViewById(R.id.recyclerView)
        productAdapter = ProductAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = productAdapter

        // Initialize Retrofit
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val storeId = sharedPref.getString("StoreID", null)

        if (storeId != null) {
            // Initialize Retrofit and make a network call
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080") // Replace with your actual base URL
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            val apiService = retrofit.create(ApiService::class.java)

            // Make a network call to get the products for the specific store ID
            apiService.getProductsByStoreId(storeId).enqueue(object : Callback<List<Product>> {
                override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                    if (response.isSuccessful) {
                        val products = response.body().orEmpty()
                        displayProductsGroupedByBrand(products)
                    } else {
                        Log.e("MainActivity", "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                    Log.e("MainActivity", "Failure: ${t.message}")
                }
            })
        }
        else {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun displayProductsGroupedByBrand(products: List<Product>) {
        val productsByBrand = products.groupBy { it.brand_name }
        val productItems = mutableListOf<ProductItem>()

        for ((brand, productsOfBrand) in productsByBrand) {
            productItems.add(ProductItem.Header(brand))
            productsOfBrand.mapTo(productItems) { ProductItem.Item(it) }
        }

        productAdapter.updateData(productItems)
    }
}