package com.geeboff.cloudjammer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geeboff.cloudjammer.MyApplication
import com.geeboff.cloudjammer.R
import com.geeboff.cloudjammer.adapter.CustomProductAdapter
import com.geeboff.cloudjammer.model.NavProductGroup
import com.geeboff.cloudjammer.repository.CustomProductRepository
import com.geeboff.cloudjammer.viewModel.CustomProductsViewModel
import com.geeboff.cloudjammer.viewModel.CustomProductsViewModelFactory

class CustomProductsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var customProductAdapter: CustomProductAdapter
    private val viewModel: CustomProductsViewModel by viewModels {
        CustomProductsViewModelFactory(CustomProductRepository(MyApplication.getApiService()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_custom_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.customRecyclerView)
        setupRecyclerView()

        val tableName = arguments?.getString("tableName") ?: return
        fetchCustomProducts(tableName)
        observeViewModel()
    }

    private fun setupRecyclerView() {
        customProductAdapter = CustomProductAdapter(requireContext(), listOf())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = customProductAdapter
    }

    private fun fetchCustomProducts(tableName: String) {
        viewModel.loadCustomProducts(tableName)
    }

    private fun observeViewModel() {
        viewModel.customProducts.observe(viewLifecycleOwner) { products ->
            customProductAdapter.updateProducts(products)
        }

        // Handle errors as well
    }
}
