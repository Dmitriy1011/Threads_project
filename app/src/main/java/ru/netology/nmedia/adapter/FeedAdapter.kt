package ru.netology.nmedia.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdvertismentBinding
import ru.netology.nmedia.databinding.CardDateBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Advertisment
import ru.netology.nmedia.dto.DateSeparator
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.handler.load
import ru.netology.nmedia.handler.loadAttachmentImage

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onUnLike(post: Post)
    fun onShowImageAsSeparate(post: Post)
}

class FeedAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(FeedDiffCallback()) {

    private val typeAdvertisment = 0
    private val typePost = 1
    private val typeDate = 2

    //получаем тип элемента из данных

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Advertisment -> typeAdvertisment
            is Post -> typePost
            is DateSeparator -> typeDate
            null -> error("unknown item type")
        }

    //создание ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            typePost -> PostViewHolder(
                CardPostBinding.inflate(layoutInflater, parent, false),
                onInteractionListener
            )

            typeAdvertisment -> {
                AdvertismentViewHolder(
                    CardAdvertismentBinding.inflate(layoutInflater, parent, false),
                    onInteractionListener
                )
            }

            typeDate -> {
                DateViewHolder(
                    CardDateBinding.inflate(layoutInflater, parent, false),
                    onInteractionListener
                )
            }

            else -> error("unknown viewtype: $viewType")
        }
    }


    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if(payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.forEach {
                (it as? Payload)?.let { payload ->
                    (holder as PostViewHolder).bindPayload(payload)
                }
            }
        }
    }

    //заполнение ViewHolder данными
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Advertisment -> (holder as AdvertismentViewHolder).bind(item)   //здесь происходит приведение типов вьюхолдер
            is Post -> (holder as PostViewHolder).bind(item)
            is DateSeparator -> (holder as DateViewHolder).bind(item)
            null -> error("unknown item type")
        }
    }
}


//избавление от мерцания
data class Payload(
    val likedByMe: Boolean? = null,
    val content: String? = null
)

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    private var index = 0

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
//            published.text = post.published
            content.text = post.content
            // в адаптере
            like.isChecked = post.likedByMe
            like.text = "${post.likes}"

            binding.attachmentImage.isVisible = !post.attachment?.url.isNullOrBlank()

            menu.isVisible = post.ownedByMe

            var url = "${BuildConfig.BASE_URL}avatars/${post.authorAvatar}"
            var attachmentUrl = "${BuildConfig.BASE_URL}media/${post.attachment?.url}"

            Log.d("url: ", url)
            Log.d("attachmentUrl: ", attachmentUrl)

            binding.attachmentImage.loadAttachmentImage(attachmentUrl)

            attachmentImage.setOnClickListener {
                onInteractionListener.onShowImageAsSeparate(post)
            }

            binding.avatar.load(url)

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                if (!post.likedByMe) onInteractionListener.onLike(post) else onInteractionListener.onUnLike(
                    post
                )
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
        }
    }

    fun bindPayload(payload: Payload) {
        payload.likedByMe?.let {
            binding.like.isChecked = it
        }

        payload.content.let {
            binding.content.text = it
        }
    }
}


class AdvertismentViewHolder(
    private val binding: CardAdvertismentBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Advertisment) {
        binding.advertismentImage.load("${BuildConfig.BASE_URL}media/${ad.image}")
    }
}

class DateViewHolder(
    private val binding: CardDateBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(separator: DateSeparator) {
        val resource = when (separator.type) {
            DateSeparator.Type.TODAY -> R.string.today
            DateSeparator.Type.YESTERDAY -> R.string.yesterday
            DateSeparator.Type.WEEK_AGO -> R.string.weekAgo
        }

        binding.root.setText(resource)
    }
}

class FeedDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) { //сверяем классы элементов, чтобы не приравнять пост к рекламе
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: FeedItem, newItem: FeedItem): Any {
        val oldOldItem = oldItem as? Post
        val newNewItem = newItem as? Post
        return Payload(
            likedByMe = newNewItem?.likedByMe.takeIf { it != oldOldItem?.likedByMe },
            content = newNewItem?.content.takeIf { it != oldOldItem?.content }
        )
    }
}
