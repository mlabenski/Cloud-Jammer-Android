package com.geeboff.cloudjammer.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geeboff.cloudjammer.model.Product
import com.geeboff.cloudjammer.repository.ProductRepository
import kotlinx.coroutines.launch

class CJProductsViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadCJProducts(storeId: String) {
        viewModelScope.launch {
            val result = repository.getProductsByStoreId(storeId)
            if (result.isSuccess) {
                _products.postValue(result.getOrNull())
            } else {
                _error.postValue(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }
}