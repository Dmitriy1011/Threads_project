package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    private val _postEdited = SingleLiveEvent<Unit>()
    val postEdited: LiveData<Unit>
        get() = _postEdited

    init {
        loadPosts()
    }

    fun loadPosts() {
        //начинаем загрузку
        _data.value = FeedModel(loading = true)
        //данные успешно получены
        repository.getAllAsync(object : PostRepository.RepositoryCallback<List<Post>> {
            override fun onSuccess(value: List<Post>) {
                _data.postValue(FeedModel(posts = value, empty = value.isEmpty(), loading = false))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.RepositoryCallback<Post> {
                override fun onSuccess(value: Post) {
                    _postCreated.postValue(Unit)
                }

                override fun onError(value: Exception) {

                }
            })
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
        //Не папку, а содержимое папки :) Переделать? Лучше да, и google-services лучше не заливать так напрямую, можно пока закомментировать вызов сервиса в AppActivity, чтобы не заморачиваться с секретами
    }

    fun likeById(post: Post) { //вызывается из FeedFragment adapter

        val old = _data.value?.posts.orEmpty()

        _data.postValue(
            _data.value?.copy(
                posts = _data.value?.posts.orEmpty().map {
                    if (it.id == post.id) post.copy(
                        likedByMe = !post.likedByMe,
                        likes = if (post.likedByMe) post.likes - 1 else post.likes + 1
                    ) else it
                })
        )

        repository.likeByIdAsync(post, object : PostRepository.RepositoryCallback<Post> {

            override fun onSuccess(value: Post) {
                _data.postValue(
                    _data.value?.copy(
                        posts = _data.value?.posts.orEmpty()
                            .map { if (it.id == post.id) value else it })
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old)).also { _data::postValue }
            }
        })
    }

    fun removeById(post: Post, id: Long) {

        val old = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            )
        )

        repository.removeByIdAsync(post, object : PostRepository.RepositoryCallback<Unit> {
            override fun onSuccess(value: Unit) {
                _data.postValue(
                    _data.value?.copy(
                        posts = _data.value?.posts.orEmpty().filter { it.id != post.id }
                    )
                )
            }

            override fun onError(value: Exception) {
                _data.postValue(_data.value?.copy(posts = old)).also { _data::postValue }
            }
        })
    }
}
