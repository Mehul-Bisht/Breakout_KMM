package com.example.breakout_kmm.android

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.breakout_kmm.Greeting
import com.example.breakout_kmm.android.showcount.core.presentation.AndroidShowCountViewModel
import com.example.breakout_kmm.android.showcount.core.presentation.components.Ball
import com.example.breakout_kmm.android.showcount.core.presentation.components.Brick
import com.example.breakout_kmm.android.showcount.core.presentation.components.Paddle
import com.example.breakout_kmm.showcount.core.presentation.Constants.BRICK_HEIGHT_MULTIPLIER
import com.example.breakout_kmm.showcount.core.presentation.Constants.BRICK_WIDTH_MULTIPLIER
import com.example.breakout_kmm.showcount.core.presentation.DialogState
import com.example.breakout_kmm.showcount.core.presentation.ShowCountViewModel
import dagger.hilt.android.AndroidEntryPoint

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColors(
            primary = Color(0xFFBB86FC),
            primaryVariant = Color(0xFF3700B3),
            secondary = Color(0xFF03DAC5)
        )
    } else {
        lightColors(
            primary = Color(0xFF6200EE),
            primaryVariant = Color(0xFF3700B3),
            secondary = Color(0xFF03DAC5)
        )
    }
    val typography = Typography(
        body1 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
    )
    val shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(0.dp)
    )

    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val viewModel = hiltViewModel<AndroidShowCountViewModel>()
                val gameState by viewModel.gameState.collectAsState()

                val density = LocalDensity.current
                val configuration = LocalConfiguration.current
                val screenWidthPx = with(density) {
                    configuration.screenWidthDp.dp.roundToPx()
                }
                val screenHeightPx = with(density) {
                    configuration.screenHeightDp.dp.roundToPx()
                }
                viewModel.setMaxScreenSize(
                    maxScreenX = screenWidthPx.toFloat(),
                    maxScreenY = screenHeightPx.toFloat(),
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color("#000000".toColorInt()))
                ) {

                    val dialogState by viewModel.dialogState.collectAsState()

                    when (dialogState) {
                        DialogState.START_GAME -> {
                            AlertDialog(
                                title = { Text(text = "Breakout Game") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.setPaddlePosition(0)
                                            viewModel.dismissDialogState()
                                        }
                                    ) {
                                        Text("Start Game", color = Color.White)
                                    }
                                },
                                dismissButton = {},
                                onDismissRequest = {}
                            )
                        }

                        DialogState.NONE -> Unit

                        DialogState.VICTORY -> {
                            AlertDialog(
                                title = { Text(text = "Congrats! You won!") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.restartGame()
                                            viewModel.dismissDialogState()
                                            viewModel.setPaddlePosition(0)
                                        }
                                    ) {
                                        Text("New game", color = Color.White)
                                    }
                                },
                                dismissButton = {},
                                onDismissRequest = {}
                            )
                        }

                        DialogState.DEFEAT -> {
                            AlertDialog(
                                title = { Text(text = "Oops! You lost!") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.restartGame()
                                            viewModel.dismissDialogState()
                                            viewModel.setPaddlePosition(0)
                                        }
                                    ) {
                                        Text("Try again", color = Color.White)
                                    }
                                },
                                dismissButton = {},
                                onDismissRequest = {}
                            )
                        }
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction = 0.85f)
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        gameState.brickPositions?.let { brickPositions ->
                            brickPositions.forEach { brickPosition ->
                                val bgPath = Path().let {
                                    it.moveTo(brickPosition.first, brickPosition.second)
                                    it.lineTo(brickPosition.first + screenWidthPx * BRICK_WIDTH_MULTIPLIER.toFloat(), brickPosition.second)
                                    it.lineTo(
                                        brickPosition.first + screenWidthPx * BRICK_WIDTH_MULTIPLIER.toFloat(),
                                        brickPosition.second + screenHeightPx * BRICK_HEIGHT_MULTIPLIER.toFloat()
                                    )
                                    it.lineTo(brickPosition.first, brickPosition.second + screenHeightPx * BRICK_HEIGHT_MULTIPLIER.toFloat())
                                    it.lineTo(brickPosition.first, brickPosition.second)
                                    it.close()
                                    it
                                }

                                drawPath(
                                    path = bgPath,
                                    brush = SolidColor(Color("#000000".toColorInt()))
                                )

                                val padding = 8f

                                val innerPath = Path().let {
                                    it.moveTo(
                                        brickPosition.first + padding,
                                        brickPosition.second + padding
                                    )
                                    it.lineTo(
                                        brickPosition.first + screenWidthPx * BRICK_WIDTH_MULTIPLIER.toFloat() - padding,
                                        brickPosition.second + padding
                                    )
                                    it.lineTo(
                                        brickPosition.first + screenWidthPx * BRICK_WIDTH_MULTIPLIER.toFloat() - padding,
                                        brickPosition.second + screenHeightPx * BRICK_HEIGHT_MULTIPLIER.toFloat() - padding
                                    )
                                    it.lineTo(
                                        brickPosition.first + padding,
                                        brickPosition.second + screenHeightPx * BRICK_HEIGHT_MULTIPLIER.toFloat() - padding
                                    )
                                    it.lineTo(
                                        brickPosition.first + padding,
                                        brickPosition.second + padding
                                    )
                                    it.close()
                                    it
                                }

                                drawPath(
                                    path = innerPath,
                                    brush = SolidColor(Color("#09DC8F".toColorInt()))
                                )
                            }
                        }

                        val paddlePath = Path().let {
                            val paddlePositionX = gameState.paddlePosition
                            val paddlePositionY = 0.75f * screenHeightPx
                            it.moveTo(paddlePositionX, paddlePositionY)
                            it.lineTo(paddlePositionX, paddlePositionY)
                            it.lineTo(paddlePositionX + 120f, paddlePositionY)
                            it.lineTo(paddlePositionX + 120f, paddlePositionY + 80f)
                            it.lineTo(paddlePositionX, paddlePositionY + 80f)
                            it.lineTo(paddlePositionX, paddlePositionY)
                            it.close()
                            it
                        }

                        drawPath(
                            path = paddlePath,
                            brush = SolidColor(Color("#07A9E7".toColorInt()))
                        )

                        drawCircle(
                            radius = 24f,
                            center = Offset(gameState.ballX, gameState.ballY),
                            brush = SolidColor(Color("#F5350C".toColorInt()))
                        )
                    }

                    //Controller Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = 0.5f)
                                .align(Alignment.CenterVertically)
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = 0.2f)
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowLeft,
                                    tint = Color.White,
                                    contentDescription = "left arrow",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = 0.6f)
                                        .pointerInput(Unit) {
                                            forEachGesture {
                                                awaitPointerEventScope {
                                                    awaitFirstDown()
                                                    viewModel.setPaddlePosition(-1)

                                                    do {
                                                        val event = awaitPointerEvent()
                                                        event.changes.forEach { pointerInputChange ->
                                                            pointerInputChange.consumePositionChange()
                                                        }
                                                    } while (event.changes.any { it.pressed })

                                                    viewModel.setPaddlePosition(0)
                                                }
                                            }
                                        }
                                )
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth()
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = 0.2f)
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowRight,
                                    tint = Color.White,
                                    contentDescription = "right arrow",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = 0.6f)
                                        .pointerInput(Unit) {
                                            forEachGesture {
                                                awaitPointerEventScope {
                                                    awaitFirstDown()
                                                    viewModel.setPaddlePosition(1)

                                                    do {
                                                        val event = awaitPointerEvent()
                                                        event.changes.forEach { pointerInputChange ->
                                                            pointerInputChange.consumePositionChange()
                                                        }
                                                    } while (event.changes.any { it.pressed })

                                                    viewModel.setPaddlePosition(0)
                                                }
                                            }
                                        }
                                )
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(text: String) {
    Text(text = text)
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        Greeting("Hello, Android!")
    }
}
