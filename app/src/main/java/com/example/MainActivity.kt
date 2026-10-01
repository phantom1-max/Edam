package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.EdamApp
import com.example.ui.EdamViewModel
import com.example.ui.theme.EdamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EdamTheme {
                val edamViewModel: EdamViewModel = viewModel()
                EdamApp(viewModel = edamViewModel)
            }
        }
    }
}
