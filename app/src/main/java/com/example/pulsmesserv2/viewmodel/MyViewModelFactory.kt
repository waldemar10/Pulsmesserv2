package com.example.pulsmesserv2.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
/*
 * Factory for ViewModels that takes Context as a dependency
 */
class MyViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}