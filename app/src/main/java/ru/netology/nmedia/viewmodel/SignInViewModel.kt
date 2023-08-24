package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl


//viewModel для авторизации
class SignInViewModel(
    private val application: Application
) : AndroidViewModel(application) {
        private val repository: PostRepository = PostRepositoryImpl(
            AppDb.getInstance(application).postDao()
        )

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