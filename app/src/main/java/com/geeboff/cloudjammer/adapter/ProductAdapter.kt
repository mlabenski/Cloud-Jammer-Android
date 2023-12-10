package com.geeboff.cloudjammer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geeboff.cloudjammer.R
import com.geeboff.cloudjammer.model.Product
import com.geeboff.cloudjammer.model.ProductItem
class ProductAdapter(private val items: MutableList<ProductItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ViewType {
        HEADER, ITEM
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ProductItem.Header -> ViewType.HEADER.ordinal
            is ProductItem.Item -> ViewType.ITEM.ordinal
        }
    }

    fun updateData(newProducts: MutableList<ProductItem>) {
        items.clear()
        items.addAll(newProducts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.HEADER.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.header_layout, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item_layout, parent, false)
                ProductViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ProductItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ProductItem.Item -> (holder as ProductViewHolder).bind(item.product, holder.itemView.context)
        }
    }

    override fun getItemCount() = items.size

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val brandName: TextView = itemView.findViewById(R.id.headerBrandName)

        fun bind(header: ProductItem.Header) {
            brandName.text = header.brandName
        }
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productDescription: TextView = itemView.findViewById(R.id.productDescription)
        private val productNicotine: TextView = itemView.findViewById(R.id.productNicotine)
        private val productSize: TextView = itemView.findViewById(R.id.productSize)
        private val productCategories: TextView = itemView.findViewById(R.id.categories)
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)

        fun bind(product: Product, context: Context) {
            productName.text = product.flavor
            // Removed the line for binding the brand name here since it's part of the header
            productDescription.text = product.description
            productNicotine.text = product.nicotine_amount
            productSize.text = product.bottle_size
            productCategories.text = product.categories
            Glide.with(context)
                .load(R.drawable.sharp_smoking_rooms_24)
                .placeholder(R.drawable.sharp_smoking_rooms_24)
                .into(productImage)
        }
    }
}
