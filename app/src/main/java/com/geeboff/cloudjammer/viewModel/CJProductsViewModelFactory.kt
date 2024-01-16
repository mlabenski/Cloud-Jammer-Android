package com.geeboff.cloudjammer.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geeboff.cloudjammer.repository.ProductRepository

class CJProductsViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CJProductsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CJProductsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
