package com.example.breakout_kmm.showcount.core.presentation

import com.example.breakout_kmm.showcount.core.presentation.Constants.BALL_SPEED_MULTIPLIER
import com.example.breakout_kmm.showcount.core.presentation.Constants.BRICK_HEIGHT_MULTIPLIER
import com.example.breakout_kmm.showcount.core.presentation.Constants.BRICK_START_MULTIPLIER
import com.example.breakout_kmm.showcount.core.presentation.Constants.BRICK_TOP_MULTIPLIER
import com.example.breakout_kmm.showcount.core.presentation.Constants.BRICK_WIDTH_MULTIPLIER
import com.example.breakout_kmm.showcount.core.presentation.Constants.PADDLE_SPEED_MULTIPLIER
import com.plcoding.translator_kmm.core.domain.util.toCommonStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ShowCountViewModel(
    private val coroutineScope: CoroutineScope?
) {

    private val _countState: MutableStateFlow<CountState> = MutableStateFlow(CountState(0))
    val countState get() = _countState.toCommonStateFlow()

    private val _gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState(10f))
    val gameState get() = _gameState.toCommonStateFlow()

    private val _gameRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val _dialogState: MutableStateFlow<DialogState> = MutableStateFlow(DialogState.START_GAME)
    val dialogState get() = _dialogState.toCommonStateFlow()

    private val _paddleDirection: MutableStateFlow<Int> = MutableStateFlow(0)

    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)
    private var isFirstInteractionRecorded = false
    private var maxScreenX: Float? = null
    private var maxScreenY: Float? = null

    private fun init() {
        setupBricks()
        startGame()
    }

    fun setMaxScreenSize(
        maxScreenX: Float,
        maxScreenY: Float
    ) {
        this.maxScreenX = maxScreenX
        this.maxScreenY = maxScreenY

        _gameState.value = _gameState.value.copy(
            ballY = 0.75f * maxScreenY - 24f
        )

        init()
    }

    fun setPaddlePosition(paddlePosition: Int) {
        when (paddlePosition) {
            -1 -> {
                movePaddleLeft()
            }

            1 -> {
                movePaddleRight()
            }

            0 -> {
                stopPaddle()
            }
        }
        if (!isFirstInteractionRecorded) {
            isFirstInteractionRecorded = true
            initBallMovement()
        }
    }

    fun dismissDialogState() {
        _dialogState.value = DialogState.NONE
    }

    private fun setupBricks() {
        val brickPositions: ArrayList<Pair<Float, Float>> = arrayListOf()
        for (i in 0..5) {
            for (j in 0..4) {
                val brickPositionX = 40f + i * maxScreenX!! * BRICK_WIDTH_MULTIPLIER.toFloat()
                val brickPositionY = 80f + j * maxScreenY!! * BRICK_HEIGHT_MULTIPLIER.toFloat()
                brickPositions.add(Pair(brickPositionX, brickPositionY))
            }
        }
        _gameState.value = _gameState.value.copy(
            brickPositions = brickPositions
        )
    }

    fun restartGame() {
        _gameRunning.value = false
        _gameState.value = GameState(10f)
        _paddleDirection.value = 0
        isFirstInteractionRecorded = false

        setupBricks()
        startGame()

        _gameState.value = _gameState.value.copy(
            ballY = 0.75f * maxScreenY!! - 24f
        )
    }

    private fun startGame() {
        if (_gameRunning.value) return
        viewModelScope.launch {
            delay(1000L)
            _gameRunning.value = true

            _paddleDirection
                .distinctUntilChanged { old, new ->
                    old == new
                }
                .combine(_gameRunning) { paddleDirection, isGameRunning ->
                    return@combine Pair(paddleDirection, isGameRunning)
                }
                .collectLatest { data ->
                    val paddleDirection = data.first
                    val isGameRunning = data.second
                    if (isGameRunning) {
                        while (true) {
                            movePaddleIfNeeded(paddleDirection)
                            moveBallIfNeeded()
                            checkForGameEnd()
                            checkForCollision()
                            delay(20L)
                        }
                    }
                }
        }
    }

    private fun endGame() {
        _gameRunning.value = false
        viewModelScope.coroutineContext.cancelChildren()
    }

    private fun movePaddleLeft() {
        _paddleDirection.value = -1
    }

    private fun movePaddleRight() {
        _paddleDirection.value = 1
    }

    private fun stopPaddle() {
        _paddleDirection.value = 0
    }

    private fun initBallMovement() {
        _gameState.value = _gameState.value.copy(
            isBallMovementInitialised = true,
            ballY = 0.75f * maxScreenY!! - 24f
        )
    }

    private fun movePaddleIfNeeded(paddleDirection: Int) {
        if (paddleDirection == -1) {
            _gameState.value = _gameState.value.copy(
                paddlePosition = _gameState.value.paddlePosition - maxScreenX!! * PADDLE_SPEED_MULTIPLIER.toFloat()
            )
        }
        if (paddleDirection == 1) {
            _gameState.value = _gameState.value.copy(
                paddlePosition = _gameState.value.paddlePosition + maxScreenX!! * PADDLE_SPEED_MULTIPLIER.toFloat()
            )
        }
    }

    private fun moveBallIfNeeded() {
        if (_gameState.value.isBallMovementInitialised) {
            _gameState.value = _gameState.value.copy(
                ballX = _gameState.value.ballX
                        + maxScreenX!! * BALL_SPEED_MULTIPLIER.toFloat() * _gameState.value.ballVelocityX,
                ballY = _gameState.value.ballY
                        + maxScreenX!! * BALL_SPEED_MULTIPLIER.toFloat() * _gameState.value.ballVelocityY
            )
        }
    }

    private fun checkForGameEnd() {
        maxScreenY?.let {
            if (_gameState.value.ballY > 0.78f * it) {
                endGame()
                _dialogState.value = DialogState.DEFEAT
            }
        }
    }

    private fun checkForCollision() {
        if (maxScreenX != null && maxScreenY != null) {
            if (_gameState.value.ballX >= maxScreenX!!) {
                _gameState.value = _gameState.value.copy(
                    ballVelocityX = -1f
                )
            } else if (_gameState.value.ballY <= 0f) {
                _gameState.value = _gameState.value.copy(
                    ballVelocityY = 1f
                )
            } else if (_gameState.value.ballX <= 0f) {
                _gameState.value = _gameState.value.copy(
                    ballVelocityX = 1f
                )
            } else if (ballCollidedWithPaddle()) {
                _gameState.value = _gameState.value.copy(
                    ballVelocityY = -1f
                )
            } else {
                removeBricksIfDestroyed()
            }
        }
    }

    private fun ballCollidedWithPaddle(): Boolean {
        return isFirstInteractionRecorded &&
                _gameState.value.ballX > _gameState.value.paddlePosition &&
                _gameState.value.ballX < _gameState.value.paddlePosition + 120f &&
                _gameState.value.ballY == 0.75f * maxScreenY!! - 24f
    }

    private fun removeBricksIfDestroyed() {
        _gameState.value.brickPositions?.let { bricks ->
            bricks.forEach { brickData ->
                val brickX = brickData.first
                val brickY = brickData.second

                if (
                    _gameState.value.ballX > brickX &&
                    _gameState.value.ballX < brickX + 120f &&
                    _gameState.value.ballY > brickY &&
                    _gameState.value.ballY < brickY + 80f
                ) {
                    val ballVelocityY = _gameState.value.ballVelocityY

                    val newBrickList = bricks.toMutableList()
                    newBrickList.remove(brickData)

                    _gameState.value = _gameState.value.copy(
                        ballVelocityY = -1f * ballVelocityY,
                        brickPositions = newBrickList
                    )

                    if (newBrickList.isEmpty()) {
                        endGame()
                        _dialogState.value = DialogState.VICTORY
                    }
                }
            }
        }
    }
}
