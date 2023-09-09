package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.Auth.AppAuth
import ru.netology.nmedia.dto.DateSeparator
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.time.LocalDateTime
import javax.inject.Inject

private val empty = Post(
    id = 0,
    authorId = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = LocalDateTime.now(),
    authorAvatar = "",
    ownedByMe = false,
)

private val today = LocalDateTime.now()
private val yesterday = today.minusDays(1)
private val weekAgo = today.minusDays(2)

fun Post?.isToday(): Boolean {
    if (this == null) return false

    return published.year == today.year && published.dayOfYear == today.dayOfYear
}

fun Post?.isYesterday(): Boolean {
    if (this == null) return false

    return published.year == yesterday.year && published.dayOfYear == yesterday.dayOfYear
}

fun Post?.isWeekAgo(): Boolean {
    if (this == null) return false

    return published.year == weekAgo.year && published.dayOfYear < weekAgo.dayOfYear
}

@HiltViewModel
@ExperimentalCoroutinesApi
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state

    private val cached: Flow<PagingData<FeedItem>> = repository
        .data
        .map { pagingData ->
            pagingData.insertSeparators(
                generator = { before, after ->
                    when {
                        before == null && after.isToday() -> {
                            DateSeparator(DateSeparator.Type.TODAY)
                        }
                        before == null || before.isToday() && after.isYesterday() -> {
                            DateSeparator(DateSeparator.Type.YESTERDAY)
                        }
                        before.isYesterday() && after.isWeekAgo() -> {
                            DateSeparator(DateSeparator.Type.YESTERDAY)
                        }
                        else -> null
                    }
                }
            )
        }.cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> =
        appAuth.authStateFlow.flatMapLatest { (_myId, _) ->
            cached.map { pagingData ->
                pagingData.map { feedItem ->
                    if (feedItem !is Post) feedItem else feedItem.copy(ownedByMe = feedItem.authorId == _myId)
                }
            }
        }

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

//
//    val newerCount: LiveData<Int> = data.switchMap {
//        val id = it.posts.firstOrNull()?.id ?: 0L
//        repository.getNewerCount().asLiveData(Dispatchers.Default)
//    }


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
