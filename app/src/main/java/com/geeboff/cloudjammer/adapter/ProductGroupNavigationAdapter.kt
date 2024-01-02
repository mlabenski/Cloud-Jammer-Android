package com.geeboff.cloudjammer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.geeboff.cloudjammer.R
import com.geeboff.cloudjammer.model.NavProductGroup

class ProductGroupNavigationAdapter(private val productGroups: List<NavProductGroup>) : RecyclerView.Adapter<ProductGroupNavigationAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(navProductGroup: NavProductGroup)
    }
    var onItemClickListener: OnItemClickListener? = null
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.productGroupImage)
        val nameView: TextView = view.findViewById(R.id.productGroupName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_group_navigation_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productGroup = productGroups[position]

        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(productGroup)
        }
        holder.nameView.text = productGroup.name

        // Check if the image is a Bitmap and set it directly
        productGroup.image?.let {
            holder.imageView.setImageBitmap(it)
        } ?: run {
            // If the image is null, set a placeholder or error image
            holder.imageView.setImageResource(R.drawable.ic_placeholder) // or error image
        }
    }

    override fun getItemCount() = productGroups.size
}
