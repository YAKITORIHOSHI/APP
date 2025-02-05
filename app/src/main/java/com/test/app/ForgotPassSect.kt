package com.test.app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ForgotPassword(onBackToLogin: () -> Unit) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    var isValidEmail by remember { mutableStateOf(false) }
    var isValidLenUsern by remember { mutableStateOf(false) }

    fun executeChanges() {
        if (!isInternetAvailable(context)) {
            Toast.makeText(context, "Internet not available. Please check your connection.", Toast.LENGTH_SHORT).show()
            return
        }

        if (username.isNotEmpty() && email.isNotEmpty()) {
            val userDataRef = firestore.collection("packsmartDBS")
                .document("users")
                .collection("regAccounts")
                .document(username)

            userDataRef.get().addOnSuccessListener { onSuccess ->
                val storedUserID = onSuccess.getString("uid") ?: ""
                val storedUsername = onSuccess.getString("username") ?: ""

                if (storedUserID.isNotEmpty()) {
                    val userVerifyRef = firestore.collection("packsmartDBS")
                        .document("users")
                        .collection("regAccounts")
                        .document(storedUsername)
                        .collection("verificationData")
                        .document(storedUserID)

                    userVerifyRef.get().addOnSuccessListener { verificationData ->
                        val storedEmail = verificationData.getString("email") ?: ""

                        if (email == storedEmail) {
                            auth.sendPasswordResetEmail(storedEmail)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Password reset email sent to $email", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val error = task.exception?.message ?: "Failed to send reset email."
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                    }
                                    onBackToLogin()
                                }
                        } else {
                            Toast.makeText(context, "Email does not match the registered email.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Both text fields must be filled.", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        ForgotBackgroundImage()

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(75.dp))

            // Email Input
            TextField(
                value = email,
                onValueChange = {
                    email = it
                    isValidEmail = checkEmail(email) // Validate on input change
                },
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
                ),
                isError = !isValidEmail // Show error state if invalid
            )

            // Username Input
            TextField(
                value = username,
                onValueChange = {
                    username = it
                    isValidLenUsern = validLength(email) // Validate on input change
                },
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
                ),
                isError = !isValidLenUsern // Show error state if invalid
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier.width(300.dp).height(150.dp).padding(top = 10.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {

                    GradientButton(
                        text = {
                            Text(
                                "Reset Password",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        },
                        onClick = { executeChanges() },
                        modifier = Modifier
                            .offset(x = 3.dp, y = 5.dp)
                            .width(300.dp)
                            .height(50.dp),
                        enabled = true
                    )

                    GradientButton(
                        text = {
                            Text(
                                "Back to Login",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        },
                        onClick = { onBackToLogin() },
                        modifier = Modifier
                            .offset(x = 3.dp, y = 5.dp)
                            .width(300.dp)
                            .height(50.dp),
                        enabled = true
                    )

                }
            }
        }
    }
}
