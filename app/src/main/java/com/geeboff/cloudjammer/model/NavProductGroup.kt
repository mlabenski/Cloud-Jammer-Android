package com.geeboff.cloudjammer.model

import android.graphics.Bitmap

data class NavProductGroup(
    val name: String,
    val fields: List<CustomField>,
    val image: Bitmap?
)
