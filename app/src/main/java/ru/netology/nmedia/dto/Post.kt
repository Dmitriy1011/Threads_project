package ru.netology.nmedia.dto

import ru.netology.nmedia.entity.AttachmentEmbeddable

//sealed означает, что FeedItem содержит всего 2 реализации: Post и Advertisment
sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String,
    val attachment: AttachmentEmbeddable? = null,
    val ownedByMe: Boolean = false
): FeedItem

data class Advertisment(
    override val id: Long,
    val image: String
): FeedItem

data class Attachment(
    val url: String,
    val description: String?,
    val type: AttachmentType
)
