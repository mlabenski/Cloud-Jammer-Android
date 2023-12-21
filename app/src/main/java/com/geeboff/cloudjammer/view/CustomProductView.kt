package com.geeboff.cloudjammer.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.geeboff.cloudjammer.R
import com.geeboff.cloudjammer.model.CustomField
import com.geeboff.cloudjammer.model.CustomProduct

class CustomProductView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.item_product, this, true)
        orientation = VERTICAL // Set the orientation of the LinearLayout
    }

    fun bindProduct(product: CustomProduct) {
        // Clear existing views
        removeAllViews()

        // Dynamically add views for each field in the product
        product.fields.forEach { field ->
            when (field.type) {
                "INTEGER", "TEXT" -> addTextView(field)
                "BLOB" -> addImageView(field)
                // Add more cases as needed
            }
        }
    }

    private fun addTextView(field: CustomField) {
        val textView = TextView(context).apply {
            text = "${field.name}: ${field.type}"
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            // Add additional styling here if needed
        }
        addView(textView)
    }

    private fun addImageView(field: CustomField) {
        val imageView = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }
        addView(imageView)
    }
}
