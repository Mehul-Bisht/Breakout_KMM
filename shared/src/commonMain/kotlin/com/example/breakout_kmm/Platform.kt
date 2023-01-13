package com.example.breakout_kmm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform