package com.geeboff.cloudjammer.adapter

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geeboff.cloudjammer.view.CustomProductView

class CustomProductAdapter(private val context: Context, private val products: List<Map<String, Any>>) : RecyclerView.Adapter<CustomProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(val productView: CustomProductView) : RecyclerView.ViewHolder(productView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        Log.d("CustomProductAdapter", "Creating ViewHolder with context: $context")

        val productView = CustomProductView(context).apply {
            layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
        }
        return ProductViewHolder(productView)
    }


    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.productView.bindProduct(products[position])
    }

    override fun getItemCount() = products.size
}
