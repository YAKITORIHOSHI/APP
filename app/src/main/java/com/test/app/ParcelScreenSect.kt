package com.test.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ParcelScreen() {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Red, Color(0xFFFFA500))
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .size(56.dp)
        ) {

            // Dropdown Menu
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.align(Alignment.BottomEnd),
                shape = RoundedCornerShape(15.dp)
            ) {
                // Dropdown Menu Items
                DropdownMenuItem(
                    text = {
                        Text(
                            "Cash on Delivery (COD)",
                            color = if (isSystemInDarkTheme()) Color.White else Color.Black
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        Toast.makeText(context, "COD Selected", Toast.LENGTH_SHORT).show()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Paid",
                            color = if (isSystemInDarkTheme()) Color(0xFFEEEEEE) else Color(0xFF323434)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        Toast.makeText(context, "Paid Selected", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            FloatingActionButton(
                onClick = { menuExpanded = true },
                containerColor = Color.Transparent,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Parcel",
                )
            }
        }
    }
}

