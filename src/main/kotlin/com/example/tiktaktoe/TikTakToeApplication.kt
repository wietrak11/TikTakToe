package com.example.tiktaktoe

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TikTakToeApplication

fun main(args: Array<String>) {
    runApplication<TikTakToeApplication>(*args)
}
