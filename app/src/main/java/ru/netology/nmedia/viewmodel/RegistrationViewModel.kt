package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.io.File

class RegistrationViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

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