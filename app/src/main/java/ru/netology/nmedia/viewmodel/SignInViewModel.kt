package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import javax.inject.Inject

@HiltViewModel
//viewModel для авторизации
class SignInViewModel @Inject constructor(
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