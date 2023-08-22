package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.Auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    authorId = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = "",
    authorAvatar = "",
    ownedByMe = false,
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

    val data: LiveData<FeedModel> = AppAuth.getInstance().state.flatMapLatest { token ->
        repository.data
            .map { posts ->
                FeedModel(posts.map {
                    it.copy(ownedByMe = it.authorId == token?.id)
                })
            }
    }.asLiveData(Dispatchers.Default)

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


    private val _photo = MutableLiveData<PhotoModel?>()
    val photo: LiveData<PhotoModel?>
        get() = _photo


    fun setPhoto(photoModel: PhotoModel) {
        _photo.value = photoModel
    }

    fun clearPhoto() {
        _photo.value = null
    }

    val newerCount: LiveData<Int> = data.switchMap {
        val id = it.posts.firstOrNull()?.id ?: 0L
        repository.getNewerCount().asLiveData(Dispatchers.Default)
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

    fun changeHiddenStatus() {
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.switchHidden()
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }


    fun save() {
        edited.value?.let {
            _postCreated.postValue(Unit)
            viewModelScope.launch {
                try {
                    _photo.value?.let { photoModel -> //запрашиваем photo из value
                        repository.saveWithAttachment(it, photoModel.file)
                    } ?: run {
                        repository.save(it)
                    }

                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
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
        edited.value =
            edited.value?.copy(content = text, authorAvatar = "")
    }

    fun likeById(id: Long) { //вызывается из FeedFragment adapter
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.likeById(id)
                _state.value = FeedModelState()
            } catch (e: Exception) {
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
            } catch (e: Exception) {
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
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

}
