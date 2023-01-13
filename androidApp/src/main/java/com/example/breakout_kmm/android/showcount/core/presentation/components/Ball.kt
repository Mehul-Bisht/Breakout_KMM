package com.example.breakout_kmm.android.showcount.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt

@Composable
fun Ball() {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(shape = RoundedCornerShape(size = 10.dp))
            .background(color = Color("#F5350C".toColorInt()))
    )
}