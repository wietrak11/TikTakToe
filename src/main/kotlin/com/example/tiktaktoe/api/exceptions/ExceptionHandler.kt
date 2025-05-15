package com.example.tiktaktoe.api.exceptions

import com.example.tiktaktoe.infrastructure.NoCurrentGameFoundException
import com.example.tiktaktoe.infrastructure.NotEmptyFieldException
import com.example.tiktaktoe.infrastructure.WrongPlayerException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {

    var logger: Logger = LoggerFactory.getLogger(ExceptionHandler::class.java)

    @ExceptionHandler(
        value = [
            WrongPlayerException::class,
            NotEmptyFieldException::class
        ]
    )
    fun handleBadRequestException(exception: Exception): ResponseEntity<String> {
        logger.error(exception.message, exception.stackTrace)
        return ResponseEntity(exception.message, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(
        NoCurrentGameFoundException::class,
    )
    fun handleNotFoundException(exception: Exception): ResponseEntity<String> {
        logger.error(exception.message, exception.stackTrace)
        return ResponseEntity(exception.message, HttpStatus.NOT_FOUND)
    }
}