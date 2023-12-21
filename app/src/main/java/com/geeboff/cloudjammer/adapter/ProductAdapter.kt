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

    var isProductDetailsVisible = true
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
            is ProductItem.Header -> {
                holder.itemView.visibility = View.VISIBLE
                (holder as HeaderViewHolder).bind(item)
            }
            is ProductItem.Item -> {
                holder.itemView.visibility = if (isProductDetailsVisible) View.VISIBLE else View.GONE
                (holder as ProductViewHolder).bind(item.product, holder.itemView.context, isProductDetailsVisible)
            }
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

        fun bind(product: Product, context: Context, isDetailsVisible: Boolean) {
            // Reset the visibility and layout parameters of the itemView
            itemView.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                if (isDetailsVisible) ViewGroup.LayoutParams.WRAP_CONTENT else 0
            )
            itemView.visibility = if (isDetailsVisible) View.VISIBLE else View.GONE

            // Based on isDetailsVisible, set the text or clear it
            productName.text = if (isDetailsVisible) product.flavor else ""
            productDescription.text = if (isDetailsVisible) product.description else ""
            productNicotine.text = if (isDetailsVisible) product.nicotine_amount else ""
            productSize.text = if (isDetailsVisible) product.bottle_size else ""
            productCategories.text = if (isDetailsVisible) product.categories else ""

            // Manage the product image visibility and content
            if (isDetailsVisible) {
                Glide.with(context)
                    .load(R.drawable.sharp_smoking_rooms_24)
                    .placeholder(R.drawable.sharp_smoking_rooms_24)
                    .into(productImage)
            } else {
                Glide.with(context).clear(productImage)
            }
        }
    }
}
