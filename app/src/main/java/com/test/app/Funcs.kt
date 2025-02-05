package com.test.app

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.view.WindowInsetsCompat.Type

fun checkEmail(email: String): Boolean {

    // Regular expression to check if the email ends with @gmail.com
    val gmailPattern = Regex("^[a-zA-Z0-9._%+-]+@gmail\\.com$")
    return gmailPattern.matches(email)

}

fun validLength(str : String): Boolean {
    return str.length >= 6
}

fun registerAndVerifyUser(email: String, password: String, auth: FirebaseAuth, callback: (Boolean, String) -> Unit) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                // Send email verification
                user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                    if (emailTask.isSuccessful) {
                        callback(true, "Registration successful! A verification email has been sent to $email. Please verify your email before logging in.")
                    } else {
                        callback(false, emailTask.exception?.message ?: "Failed to send verification email.")
                    }
                }
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
    text: @Composable () -> Unit, // A composable lambda to accept both text and other composables
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean
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
            contentPadding = PaddingValues(0.dp),
            enabled = enabled
        ) {
            text() // Directly invoke the composable lambda passed
        }
    }
}


@Composable
fun GradientCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, boxSize: Float) {
    val gradientBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFFFF914D), Color(0xFFFF3131)),
            startX = 0f,
            endX = Float.POSITIVE_INFINITY
        )
    }

    Box(
        modifier = modifier
            .width(15.dp)
            .height(15.dp)
            .offset(x = (-10).dp)
    ) {
        // Background for the gradient effect
        Canvas(modifier = Modifier.matchParentSize()) {
            if (checked) {
                drawRect(brush = gradientBrush, size = size)
            }
        }

        // Default Checkbox with transparent background
        Checkbox(
            modifier = Modifier.scale(boxSize),
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Transparent,  // Hide default checked color
                uncheckedColor = Color(0xFFFFA500),
                checkmarkColor = Color.White       // Custom checkmark color
            )
        )
    }
}

@Composable
fun getKeyboardHeight(): Int {
    val view = LocalView.current
    val keyboardHeight = remember { mutableIntStateOf(0) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val insets = ViewCompat.getRootWindowInsets(view)
            keyboardHeight.intValue = insets?.getInsets(Type.ime())?.bottom ?: 0
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    return keyboardHeight.intValue
}

@Composable
fun TextButtonWithOffset(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    phoneHeight: Float,
    onPositionChange: (Dp) -> Unit // Pass dp value instead of raw pixels
) {
    val density = LocalDensity.current // Get screen density
    val phoneY = (phoneHeight * 0.0219).toInt()

    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(5.dp),
        modifier = Modifier.onGloballyPositioned { coordinates ->
            val positionX = coordinates.positionInParent().x
            onPositionChange(with(density) { positionX.toDp() }) // Convert pixels to dp
        }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(phoneY.dp),
                tint = if (isSelected) Color.Red else Color.Gray
            )
            Text(
                text = label,
                color = if (isSelected) Color.Red else Color.Gray,
                fontSize = (phoneY - 7).sp
            )
        }
    }
}

object SharedPreferencesHelper {

    private const val PREFS_NAME = "MyPreferences"
    private const val DEFAULT_STRING_VALUE = ""
    private const val DEFAULT_BOOLEAN_VALUE = false
    private const val DEFAULT_INT_VALUE = 0

    // Function to save a string variable
    fun saveString(context: Context, key: String, value: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply() // or editor.commit()
    }

    // Function to retrieve a string variable
    fun getString(context: Context, key: String): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, DEFAULT_STRING_VALUE)
    }

    // Function to save a boolean variable
    fun saveBoolean(context: Context, key: String, value: Boolean) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply() // or editor.commit()
    }

    // Function to retrieve a boolean variable
    fun getBoolean(context: Context, key: String): Boolean {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, DEFAULT_BOOLEAN_VALUE)
    }

    // Function to save an integer variable
    fun saveInt(context: Context, key: String, value: Int) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply() // or editor.commit()
    }

    // Function to retrieve an integer variable
    fun getInt(context: Context, key: String): Int {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(key, DEFAULT_INT_VALUE)
    }
}