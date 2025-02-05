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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CreateAccount(onBackToLogin: () -> Unit, auth: FirebaseAuth) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val firestore = FirebaseFirestore.getInstance()

    var isValidEmail by remember { mutableStateOf(false) }
    var isValidLenUsern by remember { mutableStateOf(false) }
    var isValidLenUserp by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var access by remember { mutableStateOf(false) }

    fun executeChanges() {
        if (!isValidEmail) {
            Toast.makeText(context, "Invalid Email Format.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidLenUsern || !isValidLenUserp) {
            Toast.makeText(context, "Username/Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        checkCredentials(email, username) { isEmailTaken, isUsernameTaken ->
            when {
                isEmailTaken && isUsernameTaken -> {
                    Toast.makeText(context, "Both email and username are already in use.", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
                isEmailTaken -> {
                    Toast.makeText(context, "Email is already in use.", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
                isUsernameTaken -> {
                    Toast.makeText(context, "Username is already in use.", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
                else -> {
                    registerAndVerifyUser(email, password, auth) { success, message ->
                        if (success) {
                            // After registration is successful, handle the other parts like user data saving
                            val userUID = auth.currentUser?.uid
                            if (userUID == null) {
                                Toast.makeText(context, "Error: User UID is null.", Toast.LENGTH_SHORT).show()
                                isLoading = false
                                return@registerAndVerifyUser
                            }

                            val userDataRef = firestore.collection("packsmartDBS")
                                .document("users")
                                .collection("regAccounts")

                            val userDataVerif = firestore.collection("packsmartDBS")
                                .document("users")
                                .collection("regAccounts")
                                .document(username)
                                .collection("verificationData")
                                .document(userUID)

                            val userDataAuth = hashMapOf(
                                "email" to email,
                                "access" to access
                            )

                            val userData = hashMapOf(
                                "username" to username,
                                "uid" to userUID
                            )

                            userDataRef.document(username).set(userData)
                                .addOnSuccessListener {
                                    userDataVerif.set(userDataAuth)
                                        .addOnSuccessListener { success ->
                                            Toast.makeText(context, "Account created successfully! Please verify your email.", Toast.LENGTH_SHORT).show()
                                            isLoading = false
                                            onBackToLogin() // Optional: you may want to go back to login after registration
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(context, "Error saving user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                                            isLoading = false
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(context, "Error saving auth data: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                }

                        } else {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        CreateBackgroundImage()

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp),
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
                    isValidLenUsern = validLength(username) // Validate on input change
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

            // Password Input
            TextField(
                value = password,
                onValueChange = {
                    password = it
                    isValidLenUserp = validLength(password) // Validate on input change
                },
                label = { Text("Password") },
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
                isError = !isValidLenUserp // Show error state if invalid
            )

            // Create Account Button
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
                        text = {
                            Text(
                                "Create Account",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        },
                        onClick = { executeChanges() },
                        modifier = Modifier
                            .offset(x = 3.dp, y = 5.dp)
                            .width(250.dp)
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
                            .width(250.dp)
                            .height(50.dp),
                        enabled = true
                    )

                }
            }

        }
    }
}