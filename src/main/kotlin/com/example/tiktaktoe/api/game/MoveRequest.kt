package com.example.tiktaktoe.api.game

import com.example.tiktaktoe.domain.enums.Player
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class MoveRequest(

    val player: Player,
    @field:Min(value = 1, message = "Column value must be at least 1")
    @field:Max(value = 3, message = "Column value must be at most 3")
    val column: Int,
    @field:Min(value = 1, message = "Row value must be at least 1")
    @field:Max(value = 3, message = "Row value must be at most 3")
    val row: Int,
)