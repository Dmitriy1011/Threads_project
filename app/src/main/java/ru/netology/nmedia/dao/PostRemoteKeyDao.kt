package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.PostRemoteKeyEntity

//интерфейс для доступа к БД KeyRemoteKeyEntity
//запросы в таблицу
@Dao
interface PostRemoteKeyDao {
    //возвращает самый новый пост в БД
    @Query("SELECT max(`key`) FROM PostRemoteKeyEntity")
    suspend fun max(): Long?

    //возвращает самый старый пост в БД
    @Query("SELECT min(`key`) FROM PostRemoteKeyEntity")
    suspend fun min(): Long?

    //в случае возникновения конфликта мы будем перезаписывать данные в таблице
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postRemoteKeyEntity: PostRemoteKeyEntity)

    //записывает список из данных экземпляров
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postRemoteKeyEntity: List<PostRemoteKeyEntity>)

    //очистка таблицы
    @Query("DELETE FROM PostRemoteKeyEntity")
    suspend fun clear()

    //проверка, пустая таблица или нет
    @Query("SELECT COUNT(*) == 0 FROM PostRemoteKeyEntity")
    suspend fun isEmpty(): Boolean
}