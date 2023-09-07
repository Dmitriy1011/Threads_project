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
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Advertisment
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

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {

    //получаем тип элемента из данных

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Advertisment -> R.layout.card_advertisment
            is Post -> R.layout.card_post
            null -> error("unknown item type")
        }

    //создание ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }

            R.layout.card_advertisment -> {
                val binding = CardAdvertismentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AdvertismentViewHolder(binding)
            }

            else -> error("unknown viewtype: $viewType")
        }

    //заполнение ViewHolder данными
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Advertisment -> (holder as AdvertismentViewHolder).bind(item)   //здесь происходит приведение типов вьюхолдер
            is Post -> (holder as PostViewHolder).bind(item)
            null -> error("unknown item type")
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    private var index = 0

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
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
}


class AdvertismentViewHolder(
    private val binding: CardAdvertismentBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Advertisment) {
        binding.advertismentImage.load("${BuildConfig.BASE_URL}media/${ad.image}")
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) { //сверяем классы элементов, чтобы не приравнять пост к рекламе
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
