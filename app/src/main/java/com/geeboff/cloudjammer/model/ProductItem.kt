package com.geeboff.cloudjammer.model

sealed class ProductItem {
    data class Header(val brandName: String) : ProductItem()
    data class Item(val product: Product) : ProductItem()
}