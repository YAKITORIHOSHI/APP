package com.test.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Composable
fun Test(onBackToLogin: () -> Unit) {

    val context = LocalContext.current

    var result by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }

    var onClicked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val focus = LocalFocusManager.current

    if (!isInternetAvailable(context)) {
        Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        return
    }

    LaunchedEffect(onClicked) {
        if (onClicked && userEmail.isNotBlank() && checkEmail(userEmail)) {
            val emailHelper = EmailVerificationHelper()

            isLoading = true  // Show loading animation
            try {
                emailHelper.sendVerificationCode(
                    userEmail,
                    onSuccess = {
                        result = "Code sent successfully!"
                        Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                        isLoading = false  // Hide loading animation
                    },
                    onFailure = { exception ->
                        result = "Failed: ${exception.message}"
                        Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                    }
                )
            } finally {
                onClicked = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFF914D), Color(0xFFFF3131)), // Orange -> Red
                    start = Offset(Float.POSITIVE_INFINITY, 0f),
                    end = Offset(0f, 0f)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .height(400.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    TextField(
                        value = userEmail,
                        onValueChange = { userEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .heightIn(min = 56.dp, max = 56.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focus.moveFocus(FocusDirection.Down) }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Button(
                        onClick = {
                            when {
                                userEmail.isBlank() -> {
                                    Toast.makeText(context, "Please enter an email", Toast.LENGTH_SHORT).show()
                                }
                                !checkEmail(userEmail) -> {
                                    Toast.makeText(context, "Invalid Email format.", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    onClicked = true
                                    isLoading = true
                                }
                            }
                        },
                        enabled = !isLoading,  // Disable when loading
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .size(300.dp, 70.dp)
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                "Send Code",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Text(
                        text = result,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )

                    Button(
                        onClick = { onBackToLogin() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .size(300.dp, 150.dp)
                            .padding(top = 32.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray)
                    ) {
                        Text(
                            "Return to Login",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

class EmailVerificationHelper {

    private val db = FirebaseFirestore.getInstance()

    fun sendVerificationCode(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val verificationCode = (100000..999999).random().toString() // Generate 6-digit code

        // Store the code in Firestore
        val verificationData = hashMapOf(
            "email" to email,
            "code" to verificationCode,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("verification_codes").document(email)
            .set(verificationData)
            .addOnSuccessListener {
                // Launch a coroutine to send the email in the background
                CoroutineScope(Dispatchers.Main).launch {
                    sendEmail(email, verificationCode, onSuccess, onFailure)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Send Email in background thread to avoid blocking UI thread
    private suspend fun sendEmail(email: String, code: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val senderEmail = "packsmart.team.capstone@gmail.com"  // Your Gmail address
                val password = "zgap mwcx njou xbls"  // Use your Gmail password or App password

                val props = Properties().apply {
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.socketFactory.port", "465")
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.port", "465")
                }

                val session = Session.getDefaultInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(senderEmail, password)
                    }
                })

                val bannerUrl = "https://res.cloudinary.com/dqboeahnu/image/upload/f_auto,q_auto/xevnudpvkz0nlxjj6bdw"

                val htmlContent = """
                    <html>
                    <body style="text-align:center; font-family:sans-serif;">
                        <img src='$bannerUrl' width='200' height='200' alt='PackSmart Banner'/>
                        <h2 style="color:#FF3131;">Your 6-digit Verification Code</h2>
                        <p style="font-size:18px;">Your verification code is: <b>$code</b></p>
                        <p>If you didn't request this code, please ignore this email.</p>
                    </body>
                    </html>
                """.trimIndent()

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail, "PackSmart Team"))
                    addRecipient(Message.RecipientType.TO, InternetAddress(email))
                    subject = "Your 6-digit Verification Code"
                    setContent(htmlContent, "text/html; charset=utf-8") // Set content type to HTML
                }

                // ... (email setup code)
                Transport.send(message)
                // Switch to Main thread for callback
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(e)
                }
            }
        }
    }
}

fun verifyCode(email: String, enteredCode: String, onVerified: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("verification_codes").document(email)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val storedCode = document.getString("code")
                if (storedCode == enteredCode) {
                    onVerified()
                } else {
                    onFailure(Exception("Invalid verification code"))
                }
            } else {
                onFailure(Exception("No verification code found"))
            }
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}
