package com.example.tiktaktoe.api.game

import com.example.tiktaktoe.domain.enums.Player

data class MoveRequest(
    val player: Player,
    val x: Int,
    val y: Int,
)