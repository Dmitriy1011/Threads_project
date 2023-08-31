package ru.netology.nmedia.repository.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    //здесь учим daggerHilt создавать объект класса PostRepository так, чтобы его реализация была PostRepositoryImpl
    //связываем интерфейс и реализацию с помощью аннотации binds

    @Singleton
    @Binds
    fun bindsPostRepository(
        impl: PostRepositoryImpl
    ): PostRepository

}