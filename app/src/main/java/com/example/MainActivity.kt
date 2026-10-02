package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notification.EdamNotificationHelper
import com.example.ui.AuthScreen
import com.example.ui.EdamApp
import com.example.ui.EdamViewModel
import com.example.ui.theme.EdamTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EdamNotificationHelper.createNotificationChannel(this)
        setContent {
            val edamViewModel: EdamViewModel = viewModel()
            val uiState by edamViewModel.uiState.collectAsStateWithLifecycle()
            EdamTheme(themeMode = uiState.themeMode) {
                EdamAuthGate(viewModel = edamViewModel)
            }
        }
    }
}

@Composable
fun EdamAuthGate(
    viewModel: EdamViewModel
) {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
                viewModel.syncSignedInUserWithCloud()
            }
        )
    } else {
        LaunchedEffect(currentUser?.uid) {
            viewModel.syncSignedInUserWithCloud()
        }
        EdamApp(
            viewModel = viewModel,
            onSignOutComplete = {
                currentUser = null
            }
        )
    }
}
