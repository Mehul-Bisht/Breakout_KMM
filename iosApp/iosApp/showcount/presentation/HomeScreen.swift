//
//  HomeScreen.swift
//  iosApp
//
//  Created by Mehul on 22/12/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import shared

struct HomeScreen: View {
    private var title: String
    @ObservedObject var viewModel: iOSShowCountViewModel
    
    init(title: String) {
        self.title = title
        self.viewModel = iOSShowCountViewModel()
        self.screenSizeModel = ScreenSizeModel()
    }
    
    @State var canTouchDown = true
    @State var showingAlert = false
    
    class ScreenSizeModel: ObservableObject {
        @Published var isScreenSizeSet = false
        
        func markAsSet() {
            isScreenSizeSet = true
        }
        
        func isScreenSizeModelSet() -> Bool {
            return isScreenSizeSet
        }
    }
    
    @ObservedObject var screenSizeModel: ScreenSizeModel
    
    var body: some View {
        VStack (
            alignment: .center,
            spacing: 20
        ) {
            
            Canvas { context, size in
                let screenScale = UIScreen.main.scale
                let screenWidth = UIScreen.main.bounds.width
                let screenHeight = UIScreen.main.bounds.height
                
                if (!screenSizeModel.isScreenSizeModelSet()) {
                    screenSizeModel.markAsSet()
                    viewModel.setMaxScreenSize(maxScreenX: Float(screenWidth), maxScreenY: Float(screenHeight))
                }
                
                if (viewModel.gameState.brickPositions != nil) {
                    for val in viewModel.gameState.brickPositions! {
                        let brickBg = CGRect(
                            x: Double(val.first ?? 0),
                            y: Double(val.second ?? 0),
                            width: Double(screenWidth * Constants.shared.BRICK_WIDTH_MULTIPLIER),
                            height: Double(screenHeight * Constants.shared.BRICK_HEIGHT_MULTIPLIER)
                        )
                        context.fill(
                            Path(brickBg),
                            with: .color(.white)
                        )
                        
                        let brick = CGRect(
                            x: Double(val.first ?? 0).advanced(by: Double(4)),
                            y: Double(val.second ?? 0).advanced(by: Double(4)),
                            width: Double(screenWidth * Constants.shared.BRICK_WIDTH_MULTIPLIER).advanced(by: Double(-4)),
                            height: Double(screenHeight * Constants.shared.BRICK_HEIGHT_MULTIPLIER).advanced(by: Double(-4))
                        )
                        context.fill(
                            Path(brick),
                            with: .color(.green)
                        )
                    }
                }
    
                context.fill(
                    Path(
                        ellipseIn: CGRect(
                            origin: CGPoint(
                                x: Double(viewModel.gameState.ballX),
                                y: Double(viewModel.gameState.ballY)
                            ),
                            size: CGSize.init(width: 24.0, height: 24.0)
                        )
                    ),
                    with: .color(.red),
                    style: FillStyle.init(eoFill: true, antialiased: true)
                )
                
                context.fill(
                    Path(
                        CGRect(
                            x: Double(viewModel.gameState.paddlePosition),
                            y: Double(0.75 * screenHeight),
                            width: Double(110)/2,
                            height: Double(70)/2
                        )
                    ),
                    with: .color(.cyan),
                    style: FillStyle.init(eoFill: true, antialiased: true)
                )
            }
            
            HStack (
                alignment: .center,
                spacing: 20
            ) {
                
                Image(systemName: "chevron.left").controlSize(ControlSize.large)
                    .gesture(
                        DragGesture(
                            minimumDistance: 0,
                            coordinateSpace: CoordinateSpace.local
                        )
                            .onChanged({ value in
                            if (canTouchDown) {
                                viewModel.setPaddlePosition(paddlePosition: -1)
                            }
                            canTouchDown = false
                        }).onEnded({ value in
                            viewModel.setPaddlePosition(paddlePosition: 0)
                            canTouchDown = true
                        })
                    )
                
                Image(systemName: "chevron.right")
                    .gesture(
                        DragGesture(
                            minimumDistance: 0,
                            coordinateSpace: CoordinateSpace.local
                        )
                            .onChanged({ value in
                            if (canTouchDown) {
                                viewModel.setPaddlePosition(paddlePosition: 1)
                            }
                            canTouchDown = false
                        }).onEnded({ value in
                            viewModel.setPaddlePosition(paddlePosition: 0)
                            canTouchDown = true
                        })
                    )
            }
        }.onAppear {
            viewModel.startObserving()
        }.onDisappear {
            viewModel.dispose()
        }
    }
}
