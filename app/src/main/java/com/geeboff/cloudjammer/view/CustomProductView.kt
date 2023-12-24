package com.geeboff.cloudjammer.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.geeboff.cloudjammer.R

class CustomProductView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val imageView: ImageView
    private val detailsContainer: LinearLayout

    init {
        // Log before inflating the layout
        Log.d("CustomProductView", "Inflating layout for CustomProductView")

        LayoutInflater.from(context).inflate(R.layout.custom_product, this, true)

        // Log after inflating the layout
        Log.d("CustomProductView", "Layout inflated. Attempting to find views by ID")

        imageView = findViewById(R.id.custom_product_image) ?: throw AssertionError("ImageView not found")
        detailsContainer = findViewById(R.id.product_details_container) ?: throw AssertionError("Details container not found")

        // Log to confirm that the views are not null
        Log.d("CustomProductView", "Views found: ImageView: $imageView, DetailsContainer: $detailsContainer")
    }

    fun bindProduct(productData: Map<String, Any>) {
        detailsContainer.removeAllViews() // Clear previous detail views

        productData.forEach { (key, value) ->
            when (key) {
                "image" -> {
                    // If the image data is a ByteArray
                    if (value is ByteArray) {
                        val bitmap = BitmapFactory.decodeByteArray(value, 0, value.size)
                        imageView.setImageBitmap(bitmap)
                    }
                    // If the image data is a Base64 encoded string
                    else if (value is String) {
                        val bitmap = value.base64ToBitmap()
                        bitmap?.let {
                            imageView.setImageBitmap(it)
                        }
                    }
                }
                else -> {
                    val textView = TextView(context).apply {
                        text = "$key: $value"
                        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    }
                    detailsContainer.addView(textView)
                }
            }
        }
    }

    private fun addTextView(key: String, value: String) {
        val textView = TextView(context).apply {
            text = "$key: $value"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        addView(textView)
    }

    companion object {
        fun String.base64ToBitmap(): Bitmap? {
            return try {
                val decodedBytes = Base64.decode(this, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}
