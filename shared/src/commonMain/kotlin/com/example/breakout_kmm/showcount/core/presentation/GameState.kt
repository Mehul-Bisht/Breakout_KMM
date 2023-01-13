package com.example.breakout_kmm.showcount.core.presentation

data class GameState(
    val paddlePosition: Float,
    val brickPositions: List<Pair<Float, Float>>? = null,
    val isBallMovementInitialised: Boolean = false,
    val ballX: Float = 60f,
    val ballY: Float = -48f,
    val ballVelocityX: Float = 1f,
    val ballVelocityY: Float = -1f
)
