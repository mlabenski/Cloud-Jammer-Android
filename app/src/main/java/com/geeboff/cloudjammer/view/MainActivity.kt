package com.geeboff.cloudjammer.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geeboff.cloudjammer.R
import com.geeboff.cloudjammer.adapter.ProductAdapter
import com.geeboff.cloudjammer.adapter.ProductGroupNavigationAdapter
import com.geeboff.cloudjammer.api.ApiService
import com.geeboff.cloudjammer.deserializer.DynamicFieldsDeserializer
import com.geeboff.cloudjammer.model.CustomField
import com.geeboff.cloudjammer.model.NavProductGroup
import com.geeboff.cloudjammer.model.Product
import com.geeboff.cloudjammer.model.ProductItem
import com.geeboff.cloudjammer.model.Store
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
    private lateinit var searchView: SearchView
    private lateinit var categoriesSpinner: Spinner
    private lateinit var productGroupNavigationAdapter: ProductGroupNavigationAdapter
    private lateinit var productGroupNavigationRecyclerView: RecyclerView
    private var productsCache: List<Product> = listOf()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppTheme)
        setContentView(R.layout.activity_main)
        // Initialize Retrofit and make a network call
        initializeRetrofit()
        val buttonShowCustomProducts = findViewById<Button>(R.id.toggleCustomButton)
        // Click listener for button show
        buttonShowCustomProducts.setOnClickListener {
            // Create an Intent to start CustomProductDisplayActivity
            val intent = Intent(this, CustomProductDisplayActivity::class.java)
            startActivity(intent)
        }
        // Setup shared preferences
        sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)


        // Setup search view
        searchView = findViewById(R.id.searchView)
        setupSearchView()
        // Setup UI
        setupUI()
        // Initialize RecyclerView and set its layout manager
        productGroupNavigationRecyclerView = findViewById(R.id.productGroupNavigationRecyclerView)
        productGroupNavigationRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Call setupProductGroups to fetch and display the product groups
        setupProductGroups()

        val storeId = sharedPref.getString("StoreID", "1")
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
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val customGson = GsonBuilder()
            .registerTypeAdapter(object : TypeToken<Map<String, List<CustomField>>>() {}.type, DynamicFieldsDeserializer())
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.183:8080")
            .addConverterFactory(GsonConverterFactory.create(customGson))
            .client(okHttpClient)
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

        setupCategoriesSpinner();

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

        val hideProductsButton: Button = findViewById(R.id.hideProductsButton)
        hideProductsButton.setOnClickListener {
            productAdapter.isProductDetailsVisible = !productAdapter.isProductDetailsVisible
            if (productAdapter.isProductDetailsVisible) {
                hideProductsButton.text = getString(R.string.hide_products) // Update the text accordingly
            } else {
                hideProductsButton.text = getString(R.string.show_products) // Update the text accordingly
            }
            productAdapter.notifyDataSetChanged()
            recyclerView.invalidateItemDecorations()
        }
        // Set up the settings icon click listener
        val settingsIcon: ImageView = findViewById(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            // Call selectStore to show the store selection dialog
            selectStore()
        }
    }
    private fun setupProductGroups() {
        fetchProductGroups(storeId = 1) { productGroups ->
            productGroupNavigationAdapter = ProductGroupNavigationAdapter(productGroups).apply {
                onItemClickListener = object : ProductGroupNavigationAdapter.OnItemClickListener {
                    override fun onItemClick(navProductGroup: NavProductGroup) {
                        // Intent to start CustomProductDisplayActivity with the selected product group
                        val intent = Intent(this@MainActivity, CustomProductDisplayActivity::class.java)
                        intent.putExtra("productGroupName", navProductGroup.name)
                        startActivity(intent)
                    }
                }
            }
            productGroupNavigationRecyclerView.adapter = productGroupNavigationAdapter
        }
    }

    private fun fetchProductGroups(storeId: Int, callback: (List<NavProductGroup>) -> Unit) {
        lifecycleScope.launch {
            try {
                val response = apiService.getProductGroups(storeId, true)
                if (response.isSuccessful && response.body() != null) {
                    val jsonObject = response.body()!!
                    val productGroups = processResponse(jsonObject)
                    callback.invoke(productGroups)
                } else {
                    Log.e("CustomProductDisplay", "Error fetching product groupszz: ${response.errorBody()?.string()}")
                    callback.invoke(emptyList())
                }
            } catch (e: Exception) {
                Log.e("CustomProductDisplay", "Error fetching product groupsyyy", e)
                callback.invoke(emptyList())
            }
        }
    }

    // Retrofit API Response Processing
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

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchForBrand(it) }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchForBrand(it) }
                return true
            }
        })
    }

    private fun searchForBrand(query: String) {
        val filteredProducts = if (query.isNotEmpty()) {
            productsCache.filter { it.brand_name.contains(query, ignoreCase = true) }
        } else {
            productsCache
        }
        displayProductsGroupedByBrand(filteredProducts)
    }

    private fun setupCategoriesSpinner() {
        categoriesSpinner = findViewById(R.id.categoriesSpinner)
        val categories = arrayOf(
            "All Categories",
            "Salt Nic",
            "Fruit and Candy",
            "Menthol",
            "Creams and Custards",
            "Pastries and Dessert",
            "Breakfast",
            "Tobacco"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        categoriesSpinner.adapter = adapter
        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val selectedCategory = categories[position]
                filterProductsByCategory(selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed here
            }
        }
    }

        private fun filterProductsByCategory(category: String) {
            val filteredProducts = if (category == "All Categories") {
                productsCache
            }
            else {
                productsCache.filter { product ->
                    product.categories.split(",").any { it.trim().equals(category, ignoreCase = true) }
                }
            }
            displayProductsGroupedByBrand(filteredProducts)
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
        coroutineScope.launch {
            try {
                // Asynchronously fetch both responses
                val productsResponse = async { apiService.getProductsByStoreId(storeId) }

                // Await both responses
                val productsResult = productsResponse.await()

                withContext(Dispatchers.Main) {
                    if (productsResult.isSuccessful && productsResult.body() != null) {
                        displayProductsGroupedByBrand(productsResult.body()!!)
                    } else {
                        // Handle errors
                        Log.e("MainActivity", "Error fetching products")
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions such as network errors
                Log.e("MainActivity", "Error fetching data xyx", e)
            }
        }
    }


    private fun handleCustomProducts(customProducts: Map<String, List<CustomField>>) {
        customProducts.forEach { (productType, fields) ->
            // Convert the list of fields to a String for logging
            val fieldsString = fields.joinToString(separator = ", ") { field ->
                "Field(name=${field.name}, type=${field.type}, options=${field.options})"
            }

            // Log the product type and its fields
            Log.d("CustomProducts", "Product Type: $productType, Fields: [$fieldsString]")
        }
    }

    private fun selectStore() {
        coroutineScope.launch {
            try {
                val response = apiService.getStores(userID = "1923011923")

                withContext(Dispatchers.Main) {
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
            } catch (e: Exception) {
                Log.e("MainActivity", "Failure fetching stores: ${e.message}", e)
            }
        }
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