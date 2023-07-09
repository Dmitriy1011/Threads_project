package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun saveAsync(post: Post, callback: RepositoryCallback<Post>)
    fun editAsync(post: Post, callback: RepositoryCallback<Post>)
    fun removeByIdAsync(id: Long, callback: RepositoryCallback<Unit>)
    fun likeByIdAsync(id: Long, callback: RepositoryCallback<Post>)
    fun unlikeByIdAsync(id: Long, callback: RepositoryCallback<Post>)
    fun getAllAsync(callback: RepositoryCallback<List<Post>>)

    interface RepositoryCallback<T> {
        fun onSuccess(value: T)
        fun onError(value: Exception)
    }
}