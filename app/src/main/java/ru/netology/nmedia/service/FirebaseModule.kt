package ru.netology.nmedia.service

import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.AppActivity
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FirebaseModule {

    @Singleton
    @Provides
    fun provideFirebaseMessaging() : FirebaseMessaging = FirebaseMessaging.getInstance()
}