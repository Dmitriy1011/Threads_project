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

    init {
        loadPosts()
    }

    fun loadPosts() {
        thread {
            //начинаем загрузку
            _data.postValue(
                FeedModel(loading = true)
            )
            //данные успешно получены
            try {
                val posts = repository.getAll()
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty(), loading = false))
            }
            catch (e: IOException) {
                //получена ошибка
                _data.postValue(FeedModel(error = true))
            }
        }
    }

    fun save() {
        edited.value?.let {
            thread {
                repository.save(it)
                _postCreated.postValue(Unit)
            }
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
        thread {
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty().map { if(it.id == post.id) post.copy(likedByMe = !post.likedByMe, likes = if(post.likedByMe) post.likes - 1 else post.likes + 1) else it})
            )
            try {
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty().map { if(it.id == post.id) repository.likeById(post) else it})
                )

            } catch (e: IOException) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
    }

    fun removeById(id: Long) {
        thread {
            //Оптимистичная модель
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .filter { it.id != id }
                )
            )
            try {
                repository.removeById(id)
            }
            catch (e: IOException) {
                _data.postValue(
                    _data.value?.copy(posts = old)
                )
            }
        }
    }
}
