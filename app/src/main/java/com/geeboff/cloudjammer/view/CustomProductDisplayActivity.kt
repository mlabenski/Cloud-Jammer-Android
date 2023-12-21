package com.geeboff.cloudjammer.view
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.geeboff.cloudjammer.R
import com.geeboff.cloudjammer.model.CustomProduct
// Import other necessary packages

class CustomProductDisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_product_display)

        val productContainer = findViewById<LinearLayout>(R.id.productContainer)

        // Assuming you have a list of CustomProduct
        val customProducts: List<CustomProduct> = getCustomProducts() // Replace with actual data fetching logic

        customProducts.forEach { product ->
            val customProductView = CustomProductView(this)
            customProductView.bindProduct(product)
            productContainer.addView(customProductView)
        }
    }

    private fun getCustomProducts(): List<CustomProduct> {
        // Fetch or generate your list of CustomProduct here
        // For now, let's return an empty list
        return emptyList()
    }
}
