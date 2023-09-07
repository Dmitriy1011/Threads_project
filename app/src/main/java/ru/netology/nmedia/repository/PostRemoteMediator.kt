package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.exceptions.ApiError
import java.io.IOException

//класс для пагинации по страницам
//данные класс отвечает за загрузку данных по сети

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: PostApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb //экземпляр базы данных для транзакции
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                //пользователь хочет обновить список
                LoadType.REFRESH -> {
                    val max = postRemoteKeyDao.max()
                    if (max != null) {
                        apiService.getAfter(max, state.config.pageSize)
                    } else {
                        apiService.getLatest(state.config.pageSize)
                    }
                }

                //пользователь скроллит вверх
                //запрос на получение верхней страницы
                LoadType.PREPEND -> return MediatorResult.Success(true)

                //пользователь скроллит вниз
                //запрос на получение нижней страницы
                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(
                    response.code(),
                    response.message()
                )
            }

            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )


            appDb.withTransaction {
                //заполняем таблицу ключей данными, которые приходят у нас по сети
                //для этого необходимо узнать, какой был тип входных данных loadType
                //в зависимости от этого произвести запись в таблицу ключей
                when (loadType) {
                    //в случае REFRESH очищаем таблицу с постами и записываем туда оба ключа
                    LoadType.REFRESH -> {
                        if (postRemoteKeyDao.isEmpty()) {
                            postRemoteKeyDao.insert(
                                listOf(
                                    PostRemoteKeyEntity( //записываем самый первый пост из пришедшего списка
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id
                                    ),
                                    PostRemoteKeyEntity( //записываем самый последний пост из пришедшего списка
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        body.last().id
                                    )
                                )
                            )
                        } else {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                )
                            )
                        }
                    }

                    //при скролле наверх записываем ключ Before
                    LoadType.APPEND -> {
                        PostRemoteKeyEntity( //записываем самый последний пост из пришедшего списка
                            PostRemoteKeyEntity.KeyType.BEFORE,
                            body.last().id
                        )
                    }

                    else -> Unit
                }

                //используем insert для записи в БД
                postDao.insert(body.map(PostEntity::fromDto))
            }

            //!Нам нужно произвести запись нескольких таблиц в PostDao и в PostRemoteKetDao

            //здесь данные мы никуда не передаём, нам просто надо записать их в базу данных

            return MediatorResult.Success(
                body.isEmpty()
            )
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}