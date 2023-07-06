package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = "",
    authorAvatar = "",
    attachments = null,
    attachmentUrl = ""
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

    val _postCreatedError = SingleLiveEvent<Unit>()
    val postCreatedError: LiveData<Unit>
        get() = _postCreatedError


    init {
        loadPosts()
    }

    fun loadPosts() {
        //начинаем загрузку
        _data.value = FeedModel(loading = true)
        //данные успешно получены
        repository.getAllAsync(object : PostRepository.RepositoryCallback<List<Post>> {
            override fun onSuccess(value: List<Post>) {
                _data.value = FeedModel(posts = value, empty = value.isEmpty(), loading = false)
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true)
            }
        })
    }

    fun save() {
        edited.value?.let {
            _data.value = FeedModel(loading = true)
            repository.saveAsync(
                it,
                object : PostRepository.RepositoryCallback<Post> {
                    override fun onSuccess(value: Post) {
                        _data?.value?.posts?.map { value }
                    }

                    override fun onError(value: Exception) {
                        _data.value = FeedModel(error = true)
                    }

                })
            _postCreated.postValue(Unit)
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
        _data.value = FeedModel(loading = true)
        repository.editAsync(post, object : PostRepository.RepositoryCallback<Post> {
            override fun onSuccess(value: Post) {
                _data?.value?.posts?.map { value }
            }

            override fun onError(value: Exception) {
                _data.value = FeedModel(error = true)
            }
        })
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) { //вызывается из FeedFragment adapter
        _data.value = FeedModel(loading = true)
        repository.likeByIdAsync(
            id,
            object : PostRepository.RepositoryCallback<Post> {
                override fun onSuccess(value: Post) {
                    _data?.value?.posts?.map { it.likedByMe == value.likedByMe }
                }

                override fun onError(value: Exception) {
                    _data.value = FeedModel(error = true)
                }
            })
    }

    fun unlikeById(id: Long) {
        _data.value = FeedModel(loading = true)
        repository.unlikeByIdAsync(id, object : PostRepository.RepositoryCallback<Post> {
            override fun onSuccess(value: Post) {
                _data?.value?.posts?.map { it.likedByMe != value.likedByMe }
            }

            override fun onError(value: Exception) {
                _data.value = FeedModel(error = true)
            }
        })
    }


    fun removeById(id: Long) {
        _data.value = FeedModel(loading = true)
        repository.removeByIdAsync(
            id,
            object : PostRepository.RepositoryCallback<Post> {
                override fun onSuccess(value: Post) {
                    _data.value?.posts?.filter { it.id != value.id }
                }

                override fun onError(value: Exception) {
                    _data.value = FeedModel(error = true)
                }
            })
    }
}
