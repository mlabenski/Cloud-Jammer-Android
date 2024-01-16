package com.geeboff.cloudjammer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geeboff.cloudjammer.MyApplication
import com.geeboff.cloudjammer.R
import com.geeboff.cloudjammer.adapter.ProductAdapter
import com.geeboff.cloudjammer.model.Product
import com.geeboff.cloudjammer.model.ProductItem
import com.geeboff.cloudjammer.repository.ProductRepository
import com.geeboff.cloudjammer.viewModel.CJProductsViewModel
import com.geeboff.cloudjammer.viewModel.CJProductsViewModelFactory
class CJProductsFragment : Fragment() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var listViewLayoutManager: LinearLayoutManager
    private lateinit var gridViewLayoutManager: GridLayoutManager
    private var isListView = true

    // ViewModel
    private val viewModel: CJProductsViewModel by viewModels {
        CJProductsViewModelFactory(ProductRepository(MyApplication.getApiService()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cj_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        observeViewModel()
        viewModel.loadCJProducts("1")
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        productAdapter = ProductAdapter(mutableListOf())

        val safeContext = requireContext()

        listViewLayoutManager = LinearLayoutManager(safeContext)
        gridViewLayoutManager = GridLayoutManager(safeContext, 2).apply {
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
        recyclerView.layoutManager = listViewLayoutManager
        recyclerView.adapter = productAdapter

    }
    private fun toggleView() {
        isListView = !isListView
        recyclerView.layoutManager = if (isListView) listViewLayoutManager else gridViewLayoutManager
        recyclerView.adapter?.notifyDataSetChanged()
    }


    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner, { products ->
            productAdapter.updateData(products.toProductItems())
        })
    }

    // Extension function to convert List<Product> to MutableList<ProductItem>
    private fun List<Product>.toProductItems(): MutableList<ProductItem> {
        val productItems = mutableListOf<ProductItem>()

        // Grouping products by brand name
        val groupedProducts = this.groupBy { it.brand_name } // Assuming Product has a brandName field

        for ((brandName, products) in groupedProducts) {
            productItems.add(ProductItem.Header(brandName))
            products.forEach { product ->
                productItems.add(ProductItem.Item(product))
            }
        }

        return productItems
    }
}
