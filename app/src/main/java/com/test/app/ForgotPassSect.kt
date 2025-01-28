package com.test.app

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ForgotPassword(onBackToLogin: () -> Unit) {

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    // State for email input and error message
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    fun executeChanges() {

        if (!isInternetAvailable(context)) {
            Toast.makeText(context, "Internet not available. Please check your connection.", Toast.LENGTH_SHORT).show()
            return
        }

        if (username.isNotEmpty() && email.isNotEmpty()) {
            val usersCollectionRef = firestore.collection("users")

            usersCollectionRef.document(username).get().addOnSuccessListener { usernameDoc ->
                val storedUsername = usernameDoc.getString("username")
                val storedEmail = usernameDoc.getString("email")

                if (storedUsername == username && storedEmail == email) {
                    // Create or update the "password" document
                    val passwordData: Map<String, Any> = hashMapOf(
                        "password" to "0000" // Replace "0000" with the new password value
                    )

                    firestore.collection("users").document(username).update(passwordData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                            onBackToLogin() // Ensure this is only called after success
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Error updating password: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Username or email does not match our records.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(context, "Error fetching Account Info: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Both textfields must be filled.", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        ForgotBackgroundImage()

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(75.dp))

            // EMail TextField
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .heightIn(min = 56.dp, max = 56.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Username TextField
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .heightIn(min = 56.dp, max = 56.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Display error message if email doesn't match
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .width(300.dp)
                    .height(150.dp) // Adjusted height to fit both buttons
                    .padding(top = 10.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp), // Add spacing between buttons
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    GradientButton(
                        text = "Reset Password",
                        onClick = { executeChanges() },
                        modifier = Modifier
                            .width(300.dp)
                            .height(50.dp)
                    )

                    GradientButton(
                        text = "Back to Login",
                        onClick = { onBackToLogin() },
                        modifier = Modifier
                            .width(300.dp)
                            .height(50.dp)
                    )
                }
            }


        }
    }
}