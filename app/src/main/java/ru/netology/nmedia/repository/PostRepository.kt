package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<List<Post>> //по этой подписке список постов приходит из базы через viewmodel во фрагмент независимо от того, что происходит с сервером
    fun getNewerCount(): Flow<Int>
    suspend fun save(post: Post)
    suspend fun edit(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long): Post
    suspend fun unlikeById(id: Long): Post
    suspend fun getAll()
    fun switchHidden()
    suspend fun saveWithAttachment(post: Post, file: File)
}