package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
//    fun getAll(): List<Post>
//    fun likeById(post: Post): Post
//    fun save(post: Post)
    fun edit(post: Post)
//    fun removeById(id: Long)

    fun getAllAsync(callback: RepositoryCallback<List<Post>>)
    fun likeByIdAsync(post: Post, callback: RepositoryCallback<Post>)
    fun saveAsync(post: Post, callback: RepositoryCallback<Post>)
    fun removeByIdAsync(post: Post, callback: RepositoryCallback<Unit>)

    interface RepositoryCallback<T> {
        fun onSuccess(value: T)
        fun onError(value: Exception)
    }
}