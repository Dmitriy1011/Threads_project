package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
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
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state

    val data: LiveData<FeedModel> = repository.data.map(::FeedModel).asLiveData(Dispatchers.Default)
    
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

    val newerCount: LiveData<Int> = data.switchMap {
        val id = it.posts.firstOrNull()?.id ?: 0L
        repository.getNewerCount(id).asLiveData(Dispatchers.Default)
    }

    init {
        loadPosts()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = FeedModelState(refreshing = true)

            try {
                repository.getAll()

                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun loadPosts() {
        //начинаем загрузку
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.getAll()
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }


    fun save() {
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                edited.value?.let {
                    repository.save(it)
                    _state.value = FeedModelState()
                    _postCreated.postValue(Unit)
                }

                edited.value = empty
            }
            catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun edit(post: Post) {
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
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.likeById(id)
                _state.value = FeedModelState()
            }
            catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun unLikeById(id: Long) {
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.unlikeById(id)
                _state.value = FeedModelState()
            }
            catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }


    fun removeById(id: Long) {
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.removeById(id)
                _state.value = FeedModelState()
            }
            catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

}
