package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = "",
    authorAvatar = "",
    attachments = "",
    attachmentUrl = ""
)

class PostViewModel(
    application: Application,
) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()

    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data

    private val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _postEdited = SingleLiveEvent<Unit>()
    val postEdited: LiveData<Unit>
        get() = _postEdited

    private val _postsLoadError = SingleLiveEvent<String>()
    val postsLoadError: LiveData<String>
        get() = _postsLoadError

    private val _savePostError = SingleLiveEvent<String>()
    val savePostError: LiveData<String>
        get() = _savePostError


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
                _postsLoadError.value = "500 Internal Server Error. Cannot load posts"
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.saveAsync(
                it,
                object : PostRepository.RepositoryCallback<Post> {
                    override fun onSuccess(value: Post) {
                        _data?.value?.posts?.map { value }
                    }

                    override fun onError(value: Exception) {
                        _data.value = FeedModel(error = true)
                        _savePostError.value = "404 Not Found. Post cannot be created or saved"
                    }

                })
            _postCreated.postValue(Unit)
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        repository.editAsync(post, object : PostRepository.RepositoryCallback<Post> {
            override fun onSuccess(value: Post) {
                _data?.value?.posts?.map { value }
            }

            override fun onError(value: Exception) {
                _data.value = FeedModel(error = true)
            }
        })
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value =
            edited.value?.copy(content = text, authorAvatar = "", attachmentUrl = "")
    }

    fun likeById(id: Long) { //вызывается из FeedFragment adapter
        repository.likeByIdAsync(
            id,
            object : PostRepository.RepositoryCallback<Post> {
                override fun onSuccess(value: Post) {
                    _data?.value = FeedModel(posts = _data?.value?.posts?.map { if(it.id == value.id) value else it } ?: emptyList())
                }

                override fun onError(value: Exception) {
                    _data.value = FeedModel(error = true)

                }
            })
    }

    fun unLikeById(id: Long) {
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
        repository.removeByIdAsync(
            id,
            object : PostRepository.RepositoryCallback<Unit> {
                override fun onSuccess(value: Unit) {
                    _data?.value?.posts?.map { value }
                }

                override fun onError(value: Exception) {
                    _data.value = FeedModel(error = true)
                }
            })
    }
}
