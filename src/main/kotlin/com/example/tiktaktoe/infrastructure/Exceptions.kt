package com.example.tiktaktoe.infrastructure

class NoCurrentGameFoundException(msg: String) : Exception(msg)

class WrongPlayerException(msg: String) : Exception(msg)

class NotEmptyFieldException(msg: String) : Exception(msg)