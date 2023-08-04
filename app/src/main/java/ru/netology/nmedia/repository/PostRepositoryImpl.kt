package ru.netology.nmedia.repository

import android.accounts.NetworkErrorException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import java.io.IOException
import java.util.concurrent.CancellationException


class PostRepositoryImpl(
    private val dao: PostDao //база для LiveData - dao - data access object //dto - data transfer object
) : PostRepository {

    override val data: Flow<List<Post>> = dao.getAll().map(List<PostEntity>::toDto)

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            try {
                delay(10_000)

                val response = PostsApi.retrofitService.getNewer(id)

                val posts = response.body().orEmpty()

                dao.insert(posts.toEntity(true))

                emit(posts.size)
            }
            catch (e: CancellationException) {
                throw e
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getPosts()

            if (!response.isSuccessful) {
                throw RuntimeException(response.message()) //выброс ошибки означает завершение выполнения кода
            }

            val bodyWithPosts = response.body() ?: throw RuntimeException("body is null")
            dao.insert(bodyWithPosts.map(PostEntity::fromDto))
        } catch (e: IOException) {
            throw NetworkErrorException()
        }
    }


    override suspend fun likeById(id: Long): Post {
        try {
            dao.likeById(id)

            val response = PostsApi.retrofitService.likeById(id)

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            return response.body() ?: throw RuntimeException("body is null")
        } catch (e: IOException) {
            throw NetworkErrorException()
        }
    }


    override suspend fun unlikeById(id: Long): Post {
        try {
            dao.likeById(id)

            val response = PostsApi.retrofitService.unlikeById(id)

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            return response.body() ?: throw RuntimeException("body is null")
        } catch (e: IOException) {
            throw NetworkErrorException()
        }
    }

    override suspend fun edit(post: Post) {
        try {
            val response = PostsApi.retrofitService.editPost(post)

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            val result = response.body() ?: throw RuntimeException("body is null")
            dao.save(PostEntity.fromDto(result))

        } catch (e: IOException) {
            throw NetworkErrorException()
        }
    }


    override suspend fun save(post: Post) {
        try {
            val response = PostsApi.retrofitService.savePost(post)

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }

            val result = response.body() ?: throw RuntimeException("body is null")
            dao.save(PostEntity.fromDto(result))

        } catch (e: IOException) {
            throw NetworkErrorException()
        }
    }


    override suspend fun removeById(id: Long) {
        try {
            val response = PostsApi.retrofitService.deletePost(id)

            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            dao.removeById(id)
        } catch (e: IOException) {
            throw NetworkErrorException()
        }
    }
}

