package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String,
    val hidden: Boolean = false,
    @Embedded
    val attachment: AttachmentEmbeddable?
) {
    fun toDto() = Post(
        id = id,
        authorId = authorId,
        author = author,
        content = content,
        published = LocalDateTime.ofEpochSecond(published, 0, ZoneOffset.of(ZoneId.systemDefault().id)),
        likedByMe = likedByMe,
        likes = likes,
        authorAvatar = authorAvatar,
        attachment = attachment
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.content,
                dto.published.toEpochSecond(ZoneOffset.of(ZoneId.systemDefault().id)),
                dto.likedByMe,
                dto.likes,
                dto.authorAvatar,
                false,
                dto.attachment
            )
    }
}


data class AttachmentEmbeddable(
    val url: String,
    val description: String?,
    val type: AttachmentType
) {
    fun toDto() = Attachment(url, description, type)
    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.description, it.type)
        }
    }
}



fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(hidden: Boolean = false): List<PostEntity> = map(PostEntity::fromDto).map {
    it.copy(hidden = hidden)
}


