package com.geeboff.cloudjammer.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geeboff.cloudjammer.model.NavProductGroup
import com.geeboff.cloudjammer.repository.CustomProductRepository
import kotlinx.coroutines.launch

class CustomProductsViewModel(private val repository: CustomProductRepository) : ViewModel() {

    val customProducts = MutableLiveData<List<Map<String, Any>>>()
    // Error LiveData as before

    fun loadCustomProducts(tableName: String) {
        viewModelScope.launch {
            try {
                val products = repository.getCustomProducts(tableName)
                customProducts.postValue(products)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
