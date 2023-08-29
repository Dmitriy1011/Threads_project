package ru.netology.nmedia.Auth

import android.content.Context
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.workers.SendPushWorker

class AppAuth private constructor(private val context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow<Token?>(null)
    val state = _state.asStateFlow()


    //    Основной конструктор не может содержать в себе исполняемого кода. Инициализирующий код может быть помещён в соответствующие блоки (initializers blocks), которые помечаются словом init.
    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0)

        if (!prefs.contains(ID_KEY) || token == null) {
            prefs.edit { clear() }
        } else {
            _state.value = Token(id, token)
        }

        sendPushToken()
    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        prefs.edit {
            putString(TOKEN_KEY, token)
            putLong(ID_KEY, id)
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


//    Классы в Kotlin не могут иметь статических членов, ключевое слово static не входит в состав языка.
//    Можно пометить объект в классе ключевым словом companion вместе с другим ключевым словом object и обращаться к методам и свойствам объекта через имя содержащего его класса без явного указания имени объекта.

    companion object {

        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"

        private var INSTANCE: AppAuth? = null

        //2
        fun getInstance(): AppAuth =
            requireNotNull(INSTANCE) //здесь мы просто гарантируем, что AppAuth просто существует

        //1
        fun initApp(context: Context) { //в момент инициализации будем создавать объект AppAuth
            INSTANCE = AppAuth(context)
        }
    }
}