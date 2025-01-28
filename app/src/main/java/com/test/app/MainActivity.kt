package com.test.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.test.app.ui.theme.APPTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Initialize Firebase
        enableEdgeToEdge()
        setContent {
            APPTheme {
                AppScreen()
            }
        }
    }
}

@Composable
fun AppScreen() {
    var currentScreen by remember { mutableStateOf("Login") }

    when (currentScreen) {
        "Login" -> LoginScreen(
            onLoginSuccess = { currentScreen = "com.test.app.ParcelScreen" },
            onCreateAccount = { currentScreen = "com.test.app.CreateAccount" },
            onForgotPassword = { currentScreen = "com.test.app.ForgotPassword" }
        )
        "com.test.app.ForgotPassword" -> ForgotPassword(
            onBackToLogin = { currentScreen = "Login" }
        )
        "com.test.app.CreateAccount" -> CreateAccount(
            onBackToLogin = { currentScreen = "Login" },
            auth = FirebaseAuth.getInstance()
        )
        "com.test.app.ParcelScreen" -> ParcelScreen()
    }
}
