package com.test.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun checkEmail(email: String): Boolean {

    // Regular expression to check if the email ends with @gmail.com
    val gmailPattern = Regex("^[a-zA-Z0-9._%+-]+@gmail\\.com$")
    return gmailPattern.matches(email)

}

fun validLength(str : String): Boolean {
    return str.length >= 6
}

fun registerUser(email: String, password: String, auth: FirebaseAuth, callback: (Boolean, String) -> Unit) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Registration successful!")
            } else {
                callback(false, task.exception?.message ?: "Registration failed.")
            }
        }
}

fun checkCredentials(email: String, username: String, callback: (Boolean, Boolean) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()

    val userDataRef = firestore
        .collection("packsmartDBS")
        .document("users")
        .collection("regAccounts")
        .document(username)

    // Check if the username exists as a document ID
    val usernameTask = userDataRef.get()

    val uid = userDataRef.collection("uid")

    val userDataVerif = firestore
        .collection("packsmartDBS")
        .document("users")
        .collection("regAccounts")
        .document(username)
        .collection("verificationData")
        .document(uid.toString())
        .collection("email")

    // Check if the email exists within any document
    val emailQuery = userDataVerif.whereEqualTo("email", email).get()

    Tasks.whenAll(usernameTask, emailQuery)
        .addOnCompleteListener {

            val isUsernameTaken = usernameTask.result?.exists() == true
            val isEmailTaken = emailQuery.result?.documents?.isNotEmpty() == true

            callback(isEmailTaken, isUsernameTaken)
        }
        .addOnFailureListener { exception ->
            callback(false, false) // Assume both are available if there's an error
        }
}

fun isInternetAvailable(context: Context): Boolean {

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // For Android versions above API 29 (Android 10)
    val network = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
    return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFF914D), Color(0xFFFF3131)), // Orange -> Red
                    start = Offset(0f, 0f), // Top-left
                    end = Offset(Float.POSITIVE_INFINITY, 0f) // Top-right
                ),
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}
