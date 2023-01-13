package com.example.breakout_kmm.android.showcount.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt

@Composable
fun Paddle() {
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(20.dp)
            .clip(shape = RoundedCornerShape(size = 10.dp))
            .background(color = Color("#07A9E7".toColorInt()))
    )
}