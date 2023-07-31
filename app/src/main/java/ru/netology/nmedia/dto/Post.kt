package ru.netology.nmedia.dto

import java.util.Objects

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String,
    val attachments: String?,
    val attachmentUrl: String? = null,
    val localId: Long
)

