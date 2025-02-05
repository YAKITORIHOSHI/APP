package com.test.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.test.app.ui.theme.APPTheme
import android.provider.Settings
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        //Check if system is using gesture navigation
        if (!isGestureNavigation()) {
            hideSystemBars()
            GlobalVar.glob_Nav = true
        } else {
            GlobalVar.glob_Nav = false
        }

        enableEdgeToEdge()
        setContent {
            APPTheme {


                fetchRemember(this)

                if (screenDirectory == true) {
                    AppScreen("com.test.app.ParcelScreen") // Skip login if user is remembered
                } else {
                    AppScreen("Login")
                }

            }
        }
    }

    private fun isGestureNavigation(): Boolean {
        val navMode = Settings.Secure.getInt(
            contentResolver,
            "navigation_mode",
            0  // Default is 0 (Button Navigation)
        )
        return navMode == 2  // 2 means Gesture Navigation
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

}

@Composable
fun AppScreen(startScreen: String) {

    var currentScreen by remember { mutableStateOf(startScreen) }

    when (currentScreen) {
        "Login" -> LoginScreen(
            onLoginSuccess = { currentScreen = "com.test.app.ParcelScreen" },
            onCreateAccount = { currentScreen = "com.test.app.CreateAccount" },
            onForgotPassword = { currentScreen = "com.test.app.ForgotPassword" },
            onTestEmail = { currentScreen = "com.test.app.TEST" }
        )
        "com.test.app.TEST" -> Test(
            onBackToLogin = { currentScreen = "Login" }
        )
        "com.test.app.ForgotPassword" -> ForgotPassword(
            onBackToLogin = { currentScreen = "Login" }
        )
        "com.test.app.CreateAccount" -> CreateAccount(
            onBackToLogin = { currentScreen = "Login" },
            auth = FirebaseAuth.getInstance()
        )
        "com.test.app.ParcelScreen" -> ParcelScreen(
            onBackToLogin = { currentScreen = "Login" },
            onBackFromUs = { currentScreen = "com.test.app.AboutUS" },
        )
        "com.test.app.AboutUS" -> AboutUsSect(
            onBackToParcelHome = { currentScreen = "com.test.app.ParcelScreen" },
        )
    }
}