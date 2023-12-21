package com.geeboff.cloudjammer.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.geeboff.cloudjammer.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val storeIdEditText = findViewById<EditText>(R.id.storeIdEditText)
        val saveButton = findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            val storeId = storeIdEditText.text.toString()
            // Validate and save the store ID, then fetch the products
            if (storeId.isNotEmpty()) {
                // Save the store ID (e.g., SharedPreferences)
                val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("StoreID", storeId)
                    apply()
                }
                // Go to the main activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                storeIdEditText.error = "Please enter a valid store ID."
            }
        }
    }
}
