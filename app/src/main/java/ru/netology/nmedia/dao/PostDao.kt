package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity


//Query - в этой аннотации мы указали под каким именем нужный параметр должен появится в URL запроса.
//Path - нid передается не параметром, а частью пути. В этом случае нам поможет аннотация Path.
    //В строку с именем метода добавляем плэйсхолдер {id} в фигурных скобках. В параметры методов добавляем id с аннотацией Path. В этой аннотации необходимо указать, в какой плэйсхолдер надо будет подставлять значение, пришедшее в id. Указываем "id".
    //При вызове, Retrofit возьмет значение id и подставит его в строку запроса вместо {id}.
@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity WHERE hidden = 0 ORDER BY id DESC")
    fun getAllVisible(): Flow<List<PostEntity>>
    @Insert
    suspend fun insert(post: PostEntity)

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM PostEntity WHERE hidden = 1")
    suspend fun newerCount(): Int


    @Query("UPDATE PostEntity SET hidden = 0 WHERE hidden = 1")
    fun getAllInvisible()

    @Query("SELECT * FROM PostEntity ORDER BY id DESC LIMIT :count")
    fun getLatest(count: Long = 1): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query("""
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """)
    suspend fun likeById(id: Long)


    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, PostEntity>

    @Query("DELETE FROM PostEntity")
    fun clear()
}
