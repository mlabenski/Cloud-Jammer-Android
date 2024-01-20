package com.geeboff.cloudjammer.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.geeboff.cloudjammer.repository.CustomProductRepository

class CustomProductsViewModelFactory(private val repository: CustomProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomProductsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomProductsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
