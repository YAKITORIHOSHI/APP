package com.test.app

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

data class Selection(val type: String, val index: Int)

object CategoryContainer {

    var categoryHomeX: Dp = 0.dp
    var categorySearchX: Dp = 0.dp
    var categoryHistoryX: Dp = 0.dp

}

@Composable
fun ParcelScreen(onBackToLogin: () -> Unit, onBackFromUs: () -> Unit) {

    val context = LocalContext.current

    var menuExpanded by remember { mutableStateOf(false) }

    val selections = remember { mutableStateListOf<Selection>() }

    var phoneWidth = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp }
    var phoneHeight = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp }

    var animate by remember { mutableStateOf(false) }
    val animatedX by animateDpAsState(
        targetValue = if (animate) (-15).dp else phoneWidth.dp,
        animationSpec = tween(durationMillis = 400)
    )

    var test by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                test = with(density) { it.size.width.toDp() }
            }
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFF914D), Color(0xFFFF3131)), // Red -> Orange
                    start = Offset(0f, Float.POSITIVE_INFINITY), // Top
                    end = Offset(0f, 0f) // Bottom
                )
            )
    ) {

        // Floating Action Button with Menu
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-55).dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Red, Color(0xFFFFA500))
                    ),
                    shape = CircleShape
                )
                .size(70.dp)
                .zIndex(2f)
        ) {
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

        var selected by remember { mutableStateOf("Home") }
        var offsets by remember { mutableStateOf(emptyMap<String, Dp>()) }

        LaunchedEffect(CategoryContainer.categoryHomeX, CategoryContainer.categorySearchX, CategoryContainer.categoryHistoryX) {
            offsets = mapOf(
                "Home" to CategoryContainer.categoryHomeX + 12.dp,
                "Search" to CategoryContainer.categorySearchX + 11.dp,
                "History" to CategoryContainer.categoryHistoryX + 11.dp
            )
        }

        var targetOffsetX by remember { mutableStateOf(12.5.dp) }
        val animatedOffsetX by animateDpAsState(targetValue = targetOffsetX, label = "")

        LaunchedEffect(selected, offsets) {
            offsets[selected]?.let {
                targetOffsetX = it
            }
        }

        var categoryY by remember { mutableIntStateOf(0) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .onGloballyPositioned { coordinates ->
                    categoryY = coordinates.size.height
                }
                .align(Alignment.BottomCenter)
                .offset(y = (categoryY * 0.045).dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White)
                .zIndex(1f)
        ) {

            Box(
                modifier = Modifier
                    .offset(
                        x = animatedOffsetX,
                        y = (-28).dp
                    )
                    .size(63.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.2f))
                    .align(Alignment.BottomStart)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                TextButtonWithOffset(
                    label = "Home",
                    icon = Icons.Default.Home,
                    isSelected = selected == "Home",
                    onClick = { selected = "Home" },
                    phoneHeight = phoneHeight.toFloat(),
                    onPositionChange = { CategoryContainer.categoryHomeX = it }
                )

                TextButtonWithOffset(
                    label = "Search",
                    icon = Icons.Default.Search,
                    isSelected = selected == "Search",
                    onClick = { selected = "Search" },
                    phoneHeight = phoneHeight.toFloat(),
                    onPositionChange = { CategoryContainer.categorySearchX = it }
                )

                Spacer(modifier = Modifier.width(60.dp))

                TextButtonWithOffset(
                    label = "History",
                    icon = Icons.Default.AccessTime,
                    isSelected = selected == "History",
                    onClick = { selected = "History" },
                    phoneHeight = phoneHeight.toFloat(),
                    onPositionChange = { CategoryContainer.categoryHistoryX = it }
                )

                TextButtonWithOffset(
                    label = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = selected == "Settings",
                    onClick = { animate = !animate },
                    phoneHeight = phoneHeight.toFloat(),
                    onPositionChange = { 0f }
                )

            }

        }

        // Dropdown Menu Box
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-100).dp, x = (-96.5).dp)
        ) {
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier
                    .background(Color(0xFFF86404))
                    .width(200.dp)
            ) {

                DropdownMenuItem(
                    text = { Text(
                        "Cash on Delivery (COD)",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) },
                    onClick = {
                        selections.add(Selection("COD", selections.count { it.type == "COD" } + 1))
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(
                        "Paid",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) },
                    onClick = {
                        selections.add(Selection("PAID", selections.count { it.type == "PAID" } + 1))
                        menuExpanded = false
                    }
                )

            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Enables scrolling
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(775.dp) // Set desired height
                    .clip(RoundedCornerShape(15.dp))
                    .offset(y = 100.dp)
                    .background(Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .verticalScroll(rememberScrollState())
                ) {

                    /*
                    repeat(0) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(8.dp)
                                .background(
                                    color = Color(0xFFFBB03B),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Test BOX ${index + 1}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                     */

                    selections.reversed().forEach { selection ->
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp)
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(8.dp)
                                .background(
                                    color = Color(0xFFFBB03B),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${selection.type} ${selection.index}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Filler Box always at the bottom
                    Spacer(modifier = Modifier.height(100.dp))

                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = animatedX + 15.dp)
                .background(Color(0xFFE8E8E8))
                .zIndex(3f)
        ) {
            Column(

                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()

            ) {

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
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
                            .offset(y = 60.dp)
                            .fillMaxWidth()
                            .size(110.dp)
                            .background(Color.Transparent)
                    ) {
                        LogoImg(R.drawable.logo_white)
                    }
                    Box(
                        modifier = Modifier
                            .offset(y = 110.dp)
                            .fillMaxWidth()
                            .size(200.dp)
                            .background(Color.Transparent)
                    ) {
                        BrdImg(R.drawable.brand_name)
                    }
                    Box(
                        modifier = Modifier
                            .offset(x = 20.dp, y = 65.dp)
                            .size(40.dp)
                            .background(Color.Transparent)
                            .clickable { animate = !animate }
                    ) {
                        OptnImg(R.drawable.back_btn)
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(y = 25.dp)
                        .size(95.dp)
                        .background(Color.Transparent)
                ) {
                    DpImg(R.drawable.display_photo)
                }

                Text(
                    text = "My Profile",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(y = 25.dp),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .offset(y = 45.dp)
                        .width(250.dp)
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                ) {

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        GradientButton(
                            text = {
                                Text(
                                    "Account",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            },
                            onClick = { },
                            modifier = Modifier
                                .offset(x = 3.dp, y = 5.dp)
                                .width(250.dp)
                                .height(50.dp),
                            enabled = true
                        )


                        Spacer(modifier = Modifier.height(10.dp))

                        GradientButton(
                            text = {
                                Text(
                                    "Gmail",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            },
                            onClick = { },
                            modifier = Modifier
                                .offset(x = 3.dp, y = 5.dp)
                                .width(250.dp)
                                .height(50.dp),
                            enabled = true
                        )


                    }

                }

                Box(
                    modifier = Modifier
                        .offset(y = 60.dp)
                        .size((phoneWidth - 150).dp, 200.dp)
                        .background(Color.Transparent)
                ) {

                    Column (
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){

                        GradientButton(
                            text = {
                                Text(
                                    "Change Password",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            },
                            onClick = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = true
                        )


                        GradientButton(
                            text = {
                                Text(
                                    "About Us",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            },
                            onClick = { onBackFromUs() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = true
                        )

                        GradientButton(
                            text = {
                                Text(
                                    "Logout",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            },
                            onClick = {
                                onBackToLogin()
                                NaphExtra.rememberMe(context, false)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                            ,
                            enabled = true
                        )

                    }

                }

            }
        }

    }
}