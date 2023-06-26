package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
//    fun getAll(): List<Post>
//    fun likeById(post: Post): Post
//    fun save(post: Post)
//    fun edit(post: Post)
//    fun removeById(id: Long)

    fun getAllAsync(callback: RepositoryCallback<List<Post>>)
    fun likeByIdAsync(post: Post, callback: RepositoryCallback<Post>)
    fun saveAsync(post: Post, callback: RepositoryCallback<Post>)
    fun removeByIdAsync(callback: RepositoryCallback<Unit>)
    fun editAsync(post: Post, callback: RepositoryCallback<Post>)

    interface RepositoryCallback<T> {
        fun onSuccess(value: T)
        fun onError(value: Exception)
    }
}