package ru.netology.nmedia.service

import com.google.android.gms.common.GoogleApiAvailability
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import javax.inject.Singleton

@InstallIn(Singleton::class)
@Module
class GoogleApiAvailabilityModule {

    @Singleton
    @Provides
    fun provideGoogleApiAvailability(): GoogleApiAvailability = GoogleApiAvailability.getInstance()
}