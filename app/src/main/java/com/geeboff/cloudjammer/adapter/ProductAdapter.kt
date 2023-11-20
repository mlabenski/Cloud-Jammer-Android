package com.geeboff.cloudjammer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.geeboff.cloudjammer.R
import com.geeboff.cloudjammer.model.Product
import com.geeboff.cloudjammer.model.ProductItem

class ProductAdapter(private var items: MutableList<ProductItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ProductItem.Header -> TYPE_HEADER
            is ProductItem.Item -> TYPE_ITEM
        }
    }

    fun updateData(newProducts: MutableList<ProductItem>) {
        items.clear()
        items.addAll(newProducts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
                ProductViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ProductItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ProductItem.Item -> (holder as ProductViewHolder).bind(item.product)
        }
    }

    override fun getItemCount() = items.size

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val brandName: TextView = itemView.findViewById(R.id.productBrandName)

        fun bind(header: ProductItem.Header) {
            brandName.text = header.brandName
        }
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productDescription: TextView = itemView.findViewById(R.id.productDescription)
        private val productNicotine: TextView = itemView.findViewById(R.id.productNicotine)
        private val productSize: TextView = itemView.findViewById(R.id.productSize)

        fun bind(product: Product) {
            productName.text = product.flavor
            productDescription.text = product.description
            productNicotine.text = product.nicotine_amount
            productSize.text = product.bottle_size
        }
    }

}
