package ru.netology.nmedia.exceptions

class ApiError(
    code: Int,
    message: String
) : RuntimeException(message)