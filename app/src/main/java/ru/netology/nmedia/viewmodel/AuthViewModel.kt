package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import ru.netology.nmedia.Auth.AppAuth
import ru.netology.nmedia.repository.PostRepository
import javax.inject.Inject

//эта viewModel отслеживает состояние пользователя
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth,
) : ViewModel() {
    val data = appAuth.state.asLiveData(Dispatchers.Default)
    val isAuthenticated: Boolean
        get() = data.value?.token != null
}