package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.lang.Exception


class PostRepositoryImpl : PostRepository {
    override fun getAll(): List<Post> {
        return PostsApi.retrofitService.getPosts()
            .execute()
            .let {
                if (!it.isSuccessful) {
                    error("Response code is ${it.code()}")
                }
                it.body() ?: throw RuntimeException("body is null")
            }
    }

    override fun getAllAsync(callback: PostRepository.RepositoryCallback<List<Post>>) {

        PostsApi.retrofitService.getPosts()
            .enqueue(
                object : Callback<List<Post>> {
                    override fun onResponse(
                        call: Call<List<Post>>,
                        response: Response<List<Post>>
                    ) {
                        if (!response.isSuccessful) {
                            callback.onError(
                                java.lang.RuntimeException(
                                    response.errorBody()?.string()
                                )
                            )
                            return
                        }

                        val posts = response.body()

                        if (posts == null) {
                            callback.onError(RuntimeException("Body is empty"))
                            return
                        }

                        callback.onSuccess(posts)
                    }

                    override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                        callback.onError(Exception(t))
                    }

                }
            )
    }

    override fun likeById(id: Long, callback: PostRepository.RepositoryCallback<Post>) {
        PostsApi.retrofitService.likeById(id)
            .enqueue(
                object : Callback<Post> {
                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        if (!response.isSuccessful) {
                            callback.onError(
                                java.lang.RuntimeException(
                                    response.errorBody()?.string()
                                )
                            )
                        }

                        var post = response.body()

                        if (post == null) {
                            callback.onError(RuntimeException("Body is empty"))
                            return
                        }

                        callback.onSuccess(post)
                    }

                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        callback.onError(Exception(t))
                    }

                }
            )
    }

    override fun unlikeById(id: Long, callback: PostRepository.RepositoryCallback<Post>) {
        PostsApi.retrofitService.likeById(id)
            .enqueue(
                object : Callback<Post> {
                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        if (!response.isSuccessful) {
                            callback.onError(
                                java.lang.RuntimeException(
                                    response.errorBody()?.string()
                                )
                            )
                        }

                        var post = response.body()

                        if (post == null) {
                            callback.onError(RuntimeException("Body is empty"))
                            return
                        }

                        callback.onSuccess(post)
                    }

                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        callback.onError(Exception(t))
                    }

                }
            )
    }

    override fun edit(post: Post) {
        PostsApi.retrofitService.editPost(post).execute()
    }

    override fun save(post: Post, callback: PostRepository.RepositoryCallback<Post>) {
        PostsApi.retrofitService.savePost(post)
            .enqueue(
                object : Callback<Post> {
                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        if (!response.isSuccessful) {
                            callback.onError(
                                java.lang.RuntimeException(
                                    response.errorBody()?.string()
                                )
                            )
                        }

                        var post = response.body()

                        if (post == null) {
                            callback.onError(RuntimeException("Body is empty"))
                            return
                        }

                        callback.onSuccess(post)
                    }

                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        callback.onError(Exception(t))
                    }

                }
            )
    }

    override fun removeById(id: Long) {
        PostsApi.retrofitService.deletePost(id)
            .execute()
    }

}
