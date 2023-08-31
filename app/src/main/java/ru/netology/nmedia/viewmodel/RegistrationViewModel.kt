package ru.netology.nmedia.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context
) : ViewModel() {

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface PostDaoEntryPoint {
        fun getPostDao(): PostDao
    }

    private val repository: PostRepository = PostRepositoryImpl(
        EntryPointAccessors.fromApplication(context, PostDaoEntryPoint::class.java)
//        AppDb.getInstance(application).postDao()
    )

    private val _registerImage = MutableLiveData<MediaUpload>()
    val registerImage: LiveData<MediaUpload>
        get() = _registerImage

    fun setRegisterImage(media: MediaUpload) {
        _registerImage.value = media
    }

    fun saveUserWithRegister(login: String, password: String, name: String, file: File) {
        viewModelScope.launch {
            try {
                repository.registerWithAvatar(login, password, name, file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}