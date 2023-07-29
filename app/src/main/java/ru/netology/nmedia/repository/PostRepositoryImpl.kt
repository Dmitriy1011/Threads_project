package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.lang.Exception


class PostRepositoryImpl(
    private val dao: PostDao //база для LiveData - dao - data access object
) : PostRepository {

    override val data: LiveData<List<Post>> = dao.getAll().map {
        it.map(PostEntity::toDto) //приводим к объекту типа post
    }

    override suspend fun getAll() {
        val response = PostsApi.retrofitService.getPosts()

        if (!response.isSuccessful) {
            throw RuntimeException(response.message()) //выброс ошибки означает завершение выполнения кода
        }

        val bodyWithPosts = response.body() ?: throw RuntimeException("body is null")

        dao.insert(bodyWithPosts.map(PostEntity::fromDto))
    }


    override suspend fun likeById(id: Long): Post {
        val response = PostsApi.retrofitService.likeById(id)

        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }

        val bodyWithPost = response.body() ?: throw RuntimeException("body is null")

        dao.likeById(bodyWithPost.id)
        return bodyWithPost
    }


    override suspend fun unlikeById(id: Long): Post {
        val response = PostsApi.retrofitService.unlikeById(id)

        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }

        val bodyWithPost = response.body() ?: throw RuntimeException("body is null")

        dao.likeById(bodyWithPost.id)
        return bodyWithPost
    }

    override suspend fun edit(post: Post): Post {
        val response = PostsApi.retrofitService.editPost(post)

        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }

        val editedPostsBody = response.body() ?: throw RuntimeException("body is null")

        dao.save(PostEntity.fromDto(editedPostsBody))
        return editedPostsBody
    }


    override suspend fun save(post: Post): Post {
        val response = PostsApi.retrofitService.savePost(post)

        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }

        val savedPost = response.body() ?: throw RuntimeException("body is null")

        dao.save(PostEntity.fromDto(savedPost))
        return savedPost
    }


    override suspend fun removeById(id: Long) {
        val response = PostsApi.retrofitService.deletePost(id)

        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }

        dao.removeById(id)
    }
}

