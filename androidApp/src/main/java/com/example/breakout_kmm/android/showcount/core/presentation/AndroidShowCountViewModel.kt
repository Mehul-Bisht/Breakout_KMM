package com.example.breakout_kmm.android.showcount.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.breakout_kmm.showcount.core.presentation.ShowCountViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidShowCountViewModel @Inject constructor() : ViewModel() {

    private val viewModel by lazy {
        ShowCountViewModel(viewModelScope)
    }

    val countState = viewModel.countState

    val gameState = viewModel.gameState

    val dialogState = viewModel.dialogState

    fun setPaddlePosition(paddlePosition: Int) {
        viewModel.setPaddlePosition(paddlePosition)
    }

    fun setMaxScreenSize(
        maxScreenX: Float,
        maxScreenY: Float
    ) {
        viewModel.setMaxScreenSize(maxScreenX, maxScreenY)
    }

    fun dismissDialogState() {
        viewModel.dismissDialogState()
    }

    fun restartGame() {
        viewModel.restartGame()
    }
}