package ru.netology.nmedia.Auth

import android.content.Context
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.workers.SendPushWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context)
{

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow<Token?>(null)
    val state = _state.asStateFlow()

    private val idKey = "ID_KEY"
    private val tokenKey = "TOKEN_KEY"


    //    Основной конструктор не может содержать в себе исполняемого кода. Инициализирующий код может быть помещён в соответствующие блоки (initializers blocks), которые помечаются словом init.
    init {
        val token = prefs.getString(tokenKey, null)
        val id = prefs.getLong(idKey, 0)

        if (!prefs.contains(idKey) || token == null) {
            prefs.edit { clear() }
        } else {
            _state.value = Token(id, token)
        }

        sendPushToken()
    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        prefs.edit {
            putString(tokenKey, token)
            putLong(idKey, id)
        }

        _state.value = Token(id, token)
    }


    fun sendPushToken(token: String? = null) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            SendPushWorker.NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<SendPushWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    Data.Builder()
                        .putString(SendPushWorker.TOKEN_KEY, token)
                        .build()
                )
                .build()
        )
    }

    @Synchronized
    fun clearAuth() {
        with(prefs.edit()) {
            prefs.edit { clear() }
            _state.value = null
        }
        sendPushToken()
    }

    data class AuthState(val id: Long = 0, val token: String? = null)


//    Классы в Kotlin не могут иметь статических членов, ключевое слово static не входит в состав языка.
//    Можно пометить объект в классе ключевым словом companion вместе с другим ключевым словом object и обращаться к методам и свойствам объекта через имя содержащего его класса без явного указания имени объекта.

}