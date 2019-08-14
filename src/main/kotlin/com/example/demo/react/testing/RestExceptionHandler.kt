package com.example.demo.react.testing

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.IOException
import java.lang.Exception

@RestControllerAdvice
class RestExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun processInvalidIo(ex: IOException): ErrorResponse {
        return ErrorResponse(ex)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun processDefaultErrors(ex: Exception): ErrorResponse {
        return ErrorResponse(ex)
    }

    class ErrorResponse(ex: Exception) {
        var error: String? = ex.message
    }
}