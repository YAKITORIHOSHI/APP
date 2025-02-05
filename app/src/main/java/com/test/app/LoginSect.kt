package com.test.app

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onCreateAccount: () -> Unit, onForgotPassword: () -> Unit, onTestEmail: () -> Unit) {

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)

    // State variables
    var username by remember { mutableStateOf(sharedPreferences.getString("username", "") ?: "") }
    var password by remember { mutableStateOf("") }

    var emptyInput by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var noInternet by remember { mutableStateOf(false) }
    var errorLbl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var phoneWidth = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp }
    var phoneHeight = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp }

    var boxWidth by remember { mutableFloatStateOf(0f) }
    var boxHeight by remember { mutableFloatStateOf(0f) }
    var boxX by remember { mutableFloatStateOf(0f) }
    var boxY by remember { mutableFloatStateOf(0f) }

    val focusManager = LocalFocusManager.current
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()

    // Remember the state of 'rememberMe' and update it when it changes
    var isChecked by remember { mutableStateOf(NaphExtra.isRemembered(context)) }

    // Ensure the checkbox state is always updated based on changes in SharedPreferences
    val currentIsChecked by rememberUpdatedState(isChecked)

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
            errorLbl = "No internet connection.\n(Please check your network)"
            isLoading = false
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
                                    val user = firebaseAuth.currentUser

                                    // Check if the user's email is verified
                                    if (user != null && user.isEmailVerified) {
                                        if (storedAccess == true) {

                                            // Clear user data and reset login state
                                            clearUserData(sharedPreferences)
                                            username = ""
                                            password = ""

                                            if(GlobalVar.glob_Logged == true && currentIsChecked == true) {
                                                NaphExtra.rememberMe(context, true)
                                            }

                                            isLoading = false

                                            onLoginSuccess()
                                        } else {
                                            isLoading = false
                                            Toast.makeText(context, "Account Permission Denied", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(context, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    isLoading = false
                                    errorLbl = "Invalid credentials, please try again."
                                    showErrorMessage = true
                                }

                        }
                        .addOnFailureListener { exception ->
                            isLoading = false
                            Toast.makeText(context, "Error fetching Account Data: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }

                }
                .addOnFailureListener { exception ->
                    isLoading = false
                    Toast.makeText(context, "Error fetching Account Data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

        } else {

            errorLbl = "Username & Password cannot be Empty."
            emptyInput = true

        }
    }

    if(phoneHeight > 915) {

        //Tall Sized Phone
        boxX = phoneWidth * 0.075f
        boxY = phoneHeight * 0.52f
        boxWidth = phoneWidth * 0.85f
        boxHeight = phoneHeight * 0.355f

    } else if(phoneHeight > 640) {

        //Medium Sized Phone
        boxX = phoneWidth * 0.075f
        boxY = phoneHeight * 0.55f
        boxWidth = phoneWidth * 0.85f
        boxHeight = phoneHeight * 0.43f

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

        Box(
            modifier = Modifier
                .offset(y = 25.dp)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(75.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE8E8E8))
                .zIndex(1f)
        )

        LoginBgs(
            if (GlobalVar.glob_Nav == true) phoneHeight + 25
            else phoneHeight
        )

        LogoImg(R.drawable.icon_1, phoneWidth, phoneHeight)
        LblImg(R.drawable.packsmart_lbl, phoneWidth, phoneHeight)

        val keyboardHeightPx = getKeyboardHeight()
        val keyboardHeightDp = with(LocalDensity.current) { keyboardHeightPx.toDp() }

        // Animate the Y position smoothly
        val animatedOffsetY by animateDpAsState(
            targetValue = boxY.dp - (if(GlobalVar.glob_Nav == false) ((phoneHeight / 2) * 0.65f).dp else 290.dp) - (keyboardHeightDp / 1.05f),
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "animatedOffsetY"
        )

        Box(
            modifier = Modifier
                .offset(
                    y = animatedOffsetY
                )
                .align(Alignment.Center)
                .width(boxWidth.dp)
                .height(boxHeight.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(top = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-10).dp)
                        .height(35.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onForgotPassword() }
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = Color(0xFFFFA500),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Row(
                        modifier = Modifier.offset(x = (-6).dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GradientCheckbox(
                            checked = currentIsChecked, // Use updated state value
                            onCheckedChange = { newCheckedState ->
                                isChecked = newCheckedState
                                GlobalVar.glob_Logged = newCheckedState
                            },
                            boxSize = 0.8f
                        )
                        Text(
                            modifier = Modifier.offset(x = (-5).dp),
                            text = "Stay Signed In",
                            color = Color(0xFFFFA500),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                GradientButton(
                    text = {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                "Login",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    onClick = {
                        when {
                            username.isBlank() || password.isBlank() -> {
                                Toast.makeText(context, "Username & Password cannot be Empty.", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                isLoading = true
                                onLogin()
                            }
                        }
                    },
                    modifier = Modifier
                        .offset(y = (-15).dp)
                        .size(250.dp, 50.dp),
                    enabled = !isLoading
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = 4.dp, y = (-15).dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "New to this app? ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .alignByBaseline()
                            .offset(x = 6.dp),
                    )
                    TextButton(
                        onClick = { onCreateAccount() },
                        modifier = Modifier.alignByBaseline()
                    ) {
                        Text(
                            text = "Create a New Account",
                            color = Color(0xFFFFA500),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .alignByBaseline()
                                .offset(x = 3.dp)
                        )
                    }
                }

                // Error Messages
                if (showErrorMessage || noInternet || emptyInput) {
                    Toast.makeText(context, errorLbl, Toast.LENGTH_SHORT).show()
                    showErrorMessage = false
                    noInternet = false
                    emptyInput = false
                }
            }
        }

        TextButton(
            onClick = { onTestEmail() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .offset(y = (-45).dp)
                .size(150.dp, 50.dp)
                .clip(RoundedCornerShape(12.dp))
                .align(Alignment.BottomCenter)
                .zIndex(4f)
        ) {
            Text(
                "EMAIL TEST",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

    }

}

fun clearUserData(sharedPreferences: SharedPreferences) {
    val editor = sharedPreferences.edit()
    editor.remove("username")
    editor.remove("password")
    editor.apply()
}
