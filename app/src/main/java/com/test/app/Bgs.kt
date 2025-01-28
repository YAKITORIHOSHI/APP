package com.test.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun LoginBgs(w: Int, h: Int) {

    Box(
        modifier = Modifier
            .offset(
                x = 0.dp,
                y = (-15).dp
            )
            .width(w.dp)
            .height(600.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFFE8E8E8))
    )

    Box(modifier = Modifier
        .offset(
            x = 0.dp,
            y = (-15).dp
        )
        .width(w.dp)
        .height(75.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFFF914D), Color(0xFFFF3131)), // Red -> Orange
                start = Offset(Float.POSITIVE_INFINITY, 0f), // Right
                end = Offset(0f, 0f) // Left
            )
        )
    )

    Box(
        modifier = Modifier
            .offset(
                x = 0.dp,
                y = (h + 15).dp
            )
            .width(w.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE8E8E8))
    )

}

@Composable
fun ForgotBackgroundImage() {
    val background: Painter = painterResource(id = R.drawable.forgot_bg)
    Image(
        painter = background,
        contentDescription = null,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun CreateBackgroundImage() {
    val background: Painter = painterResource(id = R.drawable.create_bg)
    Image(
        painter = background,
        contentDescription = null,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun LogoImg(pngs: Int, phonex: Int, phoney: Int) {

    val background: Painter = painterResource(pngs)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter =  background,
            contentDescription = null,
            modifier = Modifier
                .offset(
                    x = (phonex * 0.04f).dp,
                    y = (phoney * 0.135f).dp
                )
                .height((phonex * 0.45f).dp)
                .width((phoney * 0.45f).dp)
        )
    }
}

@Composable
fun LblImg(pngs: Int, phonex: Int, phoney: Int) {

    val background: Painter = painterResource(pngs)

    Box(modifier = Modifier
        .offset(
            x = (phonex * 0.0001f).dp, // Adjust the x position
            y = (phoney * 0.25f).dp   // Adjust the y position
        )
        .height((phonex * 0.8f).dp)  // Adjust height
        .width((phoney * 0.9f).dp)   // Adjust width
    ) {
        Image(
            painter =  background,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()  // Make the image fill the box
                .align(Alignment.Center)  // Center the image horizontally and vertically
        )
    }
}