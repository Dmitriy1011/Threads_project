package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Post
import java.lang.Exception


class PostRepositoryImpl : PostRepository {
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

    override fun likeByIdAsync(id: Long, callback: PostRepository.RepositoryCallback<Post>) {
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

    override fun unlikeByIdAsync(id: Long, callback: PostRepository.RepositoryCallback<Post>) {
        PostsApi.retrofitService.unlikeById(id)
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


    override fun editAsync(post: Post, callback: PostRepository.RepositoryCallback<Post>) {
        PostsApi.retrofitService.editPost(post)
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


    override fun saveAsync(post: Post, callback: PostRepository.RepositoryCallback<Post>) {
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


    override fun removeByIdAsync(id: Long, callback: PostRepository.RepositoryCallback<Unit>) {
        PostsApi.retrofitService.deletePost(id)
            .enqueue(
                object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (!response.isSuccessful) {
                            callback.onError(
                                java.lang.RuntimeException(
                                    response.errorBody()?.string()
                                )
                            )
                        }

                        callback.onSuccess(Unit)
                    }


                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        callback.onError(Exception(t))
                    }
                }
            )
    }
}

