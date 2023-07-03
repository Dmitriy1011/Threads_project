package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun save(post: Post)
    fun edit(post: Post)
    fun removeById(id: Long)
    fun likeById(id: Long)
    fun unlikeById(id: Long)
    fun getAllAsync(callback: RepositoryCallback<List<Post>>)

    interface RepositoryCallback<T> {
        fun onSuccess(value: T)
        fun onError(value: Exception)
    }
}