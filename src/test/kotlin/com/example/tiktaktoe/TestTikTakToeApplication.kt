package com.example.tiktaktoe

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<TikTakToeApplication>().with(TestcontainersConfiguration::class).run(*args)
}
