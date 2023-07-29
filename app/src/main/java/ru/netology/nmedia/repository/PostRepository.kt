package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: LiveData<List<Post>> //по этой подписке список постов приходит из базы через viewmodel во фрагмент независимо от того, что происходит с сервером
    suspend fun save(post: Post): Post
    suspend fun edit(post: Post): Post
    suspend fun removeById(id: Long): Unit
    suspend fun likeById(id: Long): Post
    suspend fun unlikeById(id: Long): Post
    suspend fun getAll()
}