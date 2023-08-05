package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import retrofit2.http.POST
import ru.netology.nmedia.dto.Post
import java.util.Objects

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String,
    val attachments: String?,
    val attachmentUrl: String? = null,
    val hidden: Boolean = false
) {
    fun toDto() = Post(id, author, content, published, likedByMe, likes, authorAvatar, attachments, attachmentUrl)

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(dto.id, dto.author, dto.content, dto.published, dto.likedByMe, dto.likes, dto.authorAvatar, dto.attachments, dto.attachmentUrl)
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(hidden: Boolean = false): List<PostEntity> = map(PostEntity::fromDto).map {
    it.copy(hidden = hidden)
}


