package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.Auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl

class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

    val data = AppAuth.getInstance().state.asLiveData(Dispatchers.Default)
    val isAuthenticated: Boolean
        get() = data.value?.token != null

    fun saveIdAndToken(id: String, token: String) {
        viewModelScope.launch {
            try {
                repository.setIdAndTokenToAuth(id, token)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}