package com.geeboff.cloudjammer

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geeboff.cloudjammer.adapter.ProductAdapter
import com.geeboff.cloudjammer.api.ApiService
import com.geeboff.cloudjammer.model.Product
import com.geeboff.cloudjammer.model.ProductItem
import com.geeboff.cloudjammer.model.Store
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
    private lateinit var apiService: ApiService
    private lateinit var sharedPref: SharedPreferences
    private var storesCache: List<Store> = listOf()
    private lateinit var listViewLayoutManager: LinearLayoutManager
    private lateinit var gridViewLayoutManager: GridLayoutManager
    private var isListView = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppTheme)
        setContentView(R.layout.activity_main)

        // Setup shared preferences

        // Initialize Retrofit
        sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // Initialize Retrofit and make a network call
        initializeRetrofit()

        // Setup UI
        setupUI()

        val storeId = sharedPref.getString("StoreID", null)
        if (storeId == null) {
            print("hello i'm looking for a store ID")
            selectStore()
        }
        else {
            displayStoreName(storeId)
            loadProductsForStore(storeId)
        }
    }

    private fun initializeRetrofit() {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080") // Replace with your actual base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build())
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    private fun setupUI() {
        // set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Uncomment the following line if you want to show the title in the toolbar
        // supportActionBar?.setDisplayShowTitleEnabled(true)

        // Setup RecyclerView and its layout manager
        recyclerView = findViewById(R.id.recyclerView)
        productAdapter = ProductAdapter(mutableListOf())

        listViewLayoutManager = LinearLayoutManager(this)
        gridViewLayoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    // Headers span the full width; items only span 1 column
                    return if (productAdapter.getItemViewType(position) == ProductAdapter.ViewType.HEADER.ordinal) {
                        spanCount
                    } else {
                        1
                    }
                }
            }
        }
        // Set initial layout manager
        recyclerView = findViewById(R.id.recyclerView)
        productAdapter = ProductAdapter(mutableListOf())
        recyclerView.layoutManager = listViewLayoutManager // Start with list view
        recyclerView.adapter = productAdapter

        // Toggle button
        val toggleViewButton: Button = findViewById(R.id.toggleViewButton)
        toggleViewButton.setOnClickListener {
            isListView = !isListView
            recyclerView.layoutManager = if (isListView) {
                toggleViewButton.text = "Grid View"
                listViewLayoutManager
            } else {
                toggleViewButton.text = "List View"
                gridViewLayoutManager
            }
            productAdapter.notifyDataSetChanged()
        }

        // Set up the settings icon click listener
        val settingsIcon: ImageView = findViewById(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            // Call selectStore to show the store selection dialog
            selectStore()
        }
    }

    private fun displayProductsGroupedByBrand(products: List<Product>) {
        val productsByBrand = products.groupBy { it.brand_name }
        val sortedBrands = productsByBrand.keys.sorted()
        val productItems = mutableListOf<ProductItem>()
        for (brand in sortedBrands) {
            val productsOfBrand = productsByBrand[brand].orEmpty()
            productItems.add(ProductItem.Header(brand))
            productsOfBrand.mapTo(productItems) { ProductItem.Item(it) }
        }
        productAdapter.updateData(productItems)
    }
    private fun loadProductsForStore(storeId: String) {
        apiService.getProductsByStoreId(storeId).enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    // Successfully retrieved the list of products
                    val products = response.body().orEmpty()
                    // Display these products grouped by brand
                    displayProductsGroupedByBrand(products)
                } else {
                    // Handle the error scenario
                    Log.e("MainActivity", "Error fetching products: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                // Handle the scenario where the call failed due to network error, etc.
                Log.e("MainActivity", "Failure fetching products", t)
            }
        })
    }

    private fun selectStore() {
        // Retrofit call to fetch stores
        apiService.getStores(userID = "1923011923").enqueue(object : Callback<List<Store>> {
            override fun onResponse(call: Call<List<Store>>, response: Response<List<Store>>) {
                if (response.isSuccessful) {
                    val stores = response.body().orEmpty()
                    if (stores.isNotEmpty()) {
                        showStoresDialog(stores)
                    } else {
                        Log.e("MainActivity", "No stores found.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("MainActivity", "Error fetching stores: $errorBody")
                }
            }

            override fun onFailure(call: Call<List<Store>>, t: Throwable) {
                Log.e("MainActivity", "Failure fetching stores: ${t.message}", t)
            }
        })
    }
    // Show an AlertDialog with the list of stores for the user to select
    private fun showStoresDialog(stores: List<Store>) {
        print(stores)
        val storeNames = stores.map { it.name_dba ?: "Unnamed Store" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Select a Store")
            .setItems(storeNames) { dialog, which ->
                val selectedStoreId = stores[which].storeID
                // Save the selected store ID in SharedPreferences
                with(sharedPref.edit()) {
                    putString("StoreID", selectedStoreId.toString())
                    apply()
                }
                updateStoresCache(stores)
                displayStoreName(selectedStoreId.toString())
                // Now load the products for the selected store
                loadProductsForStore(selectedStoreId.toString())
            }
            .show()
    }
    private fun displayStoreName(storeId: String) {
        val storeName = storesCache.find { it.storeID.toString() == storeId }?.name_dba
        supportActionBar?.title = storeName ?: "Store not found"
    }

    private fun updateStoresCache(stores: List<Store>) {
        storesCache = stores
    }
}