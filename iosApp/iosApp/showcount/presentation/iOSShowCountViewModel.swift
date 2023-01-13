//
//  iOSShowCountViewModel.swift
//  iosApp
//
//  Created by Mehul on 22/12/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

extension HomeScreen {
    @MainActor class iOSShowCountViewModel: ObservableObject {
        
        private let viewModel: ShowCountViewModel
        
        init() {
            self.viewModel = ShowCountViewModel(coroutineScope: nil)
        }
        
        @Published var state: CountState = CountState(currentCount: 0)
        @Published var gameState: GameState = GameState(paddlePosition: Float(10), brickPositions: nil, isBallMovementInitialised: false, ballX: Float(0), ballY: Float(0), ballVelocityX: Float(1), ballVelocityY: Float(-1))
        @Published var dialogState: DialogState = DialogState.startGame
        
        private var gameStateHandle: DisposableHandle?
        private var dialogStateHandle: DisposableHandle?
        
        func startObserving() {
            gameStateHandle = viewModel.gameState.subscribe(onCollect: { gameState in
                if let gameState = gameState {
                    self.gameState = gameState
                }
            })
            dialogStateHandle = viewModel.dialogState.subscribe(onCollect: { dialogState in
                if let dialogState = dialogState {
                    self.dialogState = dialogState
                }
            })
        }
        
        func setMaxScreenSize(maxScreenX: Float, maxScreenY: Float) {
            viewModel.setMaxScreenSize(maxScreenX: maxScreenX, maxScreenY: maxScreenY)
        }
        
        func setPaddlePosition(paddlePosition: Int32) {
            viewModel.setPaddlePosition(paddlePosition: paddlePosition)
        }
        
        func dispose() {
            gameStateHandle?.dispose()
            dialogStateHandle?.dispose()
        }
    }
}
