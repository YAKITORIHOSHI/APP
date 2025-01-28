package com.test.app

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onCreateAccount: () -> Unit, onForgotPassword: () -> Unit) {

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)

    // State variables
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorLbl by remember { mutableStateOf("") }
    var emptyInput by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var noInternet by remember { mutableStateOf(false) }

    var phoneWidth = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp }
    var phoneHeight = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp }

    var boxWidth by remember { mutableFloatStateOf(0f) }
    var boxHeight by remember { mutableFloatStateOf(0f) }
    var boxX by remember { mutableFloatStateOf(0f) }
    var boxY by remember { mutableFloatStateOf(0f) }

    val focusManager = LocalFocusManager.current
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()

    // Reset all state variables when the app is reopened
    LaunchedEffect(Unit) {
        clearUserData(sharedPreferences)
        username = ""
        password = ""
        errorLbl = " "
        passwordVisible = false
        showErrorMessage = false
        noInternet = false
        emptyInput = false
    }

    // Validate credentials when login button is clicked
    fun onLogin() {

        if (!isInternetAvailable(context)) {
            noInternet = true
            showErrorMessage = false
            errorLbl = "   No internet connection.\n(Please check your network)"
            return
        }

        if(username != "" && password != "") {

            // If there is internet, proceed with login
            val userDataRef = firestore
                .collection("packsmartDBS")
                .document("users")
                .collection("regAccounts")
                .document(username)

            userDataRef.get()
                .addOnSuccessListener { userData ->

                    var storedUsername = userData.get("username").toString()
                    var storedUserID = userData.get("uid").toString()

                    val userVerifyRef = firestore
                        .collection("packsmartDBS")
                        .document("users")
                        .collection("regAccounts")
                        .document(storedUsername)
                        .collection("verificationData")
                        .document(storedUserID)

                    userVerifyRef.get()
                        .addOnSuccessListener { userVerified ->

                            val storedAccess = userVerified.getBoolean("access")
                            val storedEmail = userVerified.getString("email")

                            firebaseAuth.signInWithEmailAndPassword(storedEmail.toString(), password)
                                .addOnSuccessListener { onSuccess ->

                                    if(storedAccess == true) {

                                        clearUserData(sharedPreferences)

                                        username = ""
                                        password = ""

                                        onLoginSuccess()

                                    } else {
                                        Toast.makeText(context, "Account Permission Denied", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    errorLbl = "Invalid credentials, please try again.\n "
                                    showErrorMessage = true
                                }

                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Error fetching Account Data: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }

                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error fetching Account Data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

        } else {

            errorLbl = "Username & Password cannot be Empty. \n "
            emptyInput = true

        }
    }

    if(phoneHeight > 915) {

        //Tall Sized Phone
        boxX = phoneWidth * 0.075f
        boxY = phoneHeight * 0.551f
        boxWidth = phoneWidth * 0.85f
        boxHeight = phoneHeight * 0.355f

    } else if(phoneHeight > 640) {

        //Medium Sized Phone
        boxX = phoneWidth * 0.075f
        boxY = phoneHeight * 0.551f
        boxWidth = phoneWidth * 0.85f
        boxHeight = phoneHeight * 0.43f

    } else {

        //Small Sized Phone
        TODO("Not yet implemented")

    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFFF914D), Color(0xFFFF3131)), // Red -> Orange
                start = Offset(Float.POSITIVE_INFINITY, 0f), // Right
                end = Offset(0f, 0f) // Left
            )
        )
    ) {

        LoginBgs(phoneWidth, phoneHeight)

        LogoImg(R.drawable.icon_1, phoneWidth, phoneHeight)
        LblImg(R.drawable.packsmart_lbl, phoneWidth, phoneHeight)

        Box(
            modifier = Modifier
                .offset(
                    x = boxX.dp,
                    y = boxY.dp
                )
                .width(boxWidth.dp)
                .height(boxHeight.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(1.dp))

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

                // Password TextField with Visibility Toggle
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .heightIn(min = 56.dp, max = 56.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onLogin() }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    TextButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(shape = RoundedCornerShape(12.dp), color =
                                if (isSystemInDarkTheme()) Color(0xFF323434)
                                else Color.Transparent)
                            .height(56.dp)
                            .width(70.dp)
                    ) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                //LOGIN BUTTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
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
                        onClick = { onLogin() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp) // Remove padding to align with gradient
                    ) {
                        Text(
                            "Login",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-15).dp)
                        .height(35.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            onForgotPassword()
                        }
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = Color(0xFFFFA500),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    TextButton(
                        onClick = {
                            onCreateAccount()
                        }
                    ) {
                        Text(
                            text = "Create a New Account",
                            color = Color(0xFFFFA500),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Error Messages
                if (showErrorMessage) {
                    Column(
                        modifier = Modifier
                            .offset(y = (-15).dp)
                            .fillMaxWidth() // Ensures full width
                            .wrapContentHeight() // Ensures the height is just enough to fit the text
                            .padding(horizontal = 16.dp), // Optional padding
                        horizontalAlignment = Alignment.CenterHorizontally // This centers the text inside the column
                    ) {
                        Text(
                            text = errorLbl,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .heightIn(min = 20.dp, max = 50.dp) // Limit height if the message is too long
                        )
                    }
                } else if (noInternet) {
                    Column(
                        modifier = Modifier
                            .offset(y = (-15).dp)
                            .fillMaxWidth() // Ensures full width
                            .wrapContentHeight() // Ensures the height is just enough to fit the text
                            .padding(horizontal = 16.dp), // Optional padding
                        horizontalAlignment = Alignment.CenterHorizontally // This centers the text inside the column
                    ) {
                        Text(
                            text = errorLbl,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .heightIn(min = 20.dp, max = 50.dp) // Limit height if the message is too long
                        )
                    }
                } else if (emptyInput) {
                    Column(
                        modifier = Modifier
                            .offset(y = (-15).dp)
                            .fillMaxWidth() // Ensures full width
                            .wrapContentHeight() // Ensures the height is just enough to fit the text
                            .padding(horizontal = 16.dp), // Optional padding
                        horizontalAlignment = Alignment.CenterHorizontally // This centers the text inside the column
                    ) {
                        Text(
                            text = errorLbl,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .heightIn(min = 20.dp, max = 50.dp) // Limit height if the message is too long
                        )
                    }
                } else {
                    // Placeholder for the error message space (minimized height, centered)
                    Column(
                        modifier = Modifier
                            .offset(y = (-15).dp)
                            .fillMaxWidth() // Ensures full width
                            .wrapContentHeight() // Ensures the height is just enough to fit the text
                            .padding(horizontal = 16.dp) // Optional padding
                            .heightIn(min = 20.dp, max = 50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally // This centers the empty space as well
                    ) {
                        Text(
                            text = " \n ", // Empty space to reserve the space
                            fontSize = 14.sp
                        )
                    }
                }

            }
        }
    }
}

fun clearUserData(sharedPreferences: SharedPreferences) {
    val editor = sharedPreferences.edit()
    editor.remove("username")
    editor.remove("password")
    editor.apply()
}