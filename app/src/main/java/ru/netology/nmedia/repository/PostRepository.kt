package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<Post>> //по этой подписке список постов приходит из базы через viewmodel во фрагмент независимо от того, что происходит с сервером
    fun getNewerCount(): Flow<Int>
    suspend fun save(post: Post)
    suspend fun edit(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long): Post
    suspend fun unlikeById(id: Long): Post
    suspend fun getAll()
    fun switchHidden()
    suspend fun saveWithAttachment(post: Post, file: File)
    suspend fun setIdAndTokenToAuth(id: String, token: String)
    suspend fun setDataForRegistration(login: String, password: String, name: String)
    suspend fun registerWithAvatar(login: String, pass: String, name: String, file: File)
}