package ru.netology.nmedia.handler

import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding

fun ImageView.load(url: String) {

    val binding = CardPostBinding.inflate(LayoutInflater.from(context))

    Glide.with(this)
        .load(url)
        .timeout(10_000)
        .placeholder(R.drawable.baseline_crop_original_24)
        .transition(DrawableTransitionOptions.withCrossFade())
        .circleCrop()
        .error(R.drawable.baseline_error_24)
        .into(this)
//        .into(object : CustomTarget<Drawable>() {
//            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
//                val width = resource.intrinsicWidth
//                val height = resource.intrinsicHeight
//
//                val displayMetrics = binding.root.context.resources.displayMetrics
//                val screenWidth = displayMetrics.widthPixels
//
//                val calculatedHeight = (screenWidth.toFloat() / width.toFloat() * height).toInt()
//            }
//
//            override fun onLoadCleared(placeholder: Drawable?) {
//                TODO("Not yet implemented")
//            }
//
//        })
}

//Glide.with(binding.Attachment)
//.load(url)
//.timeout(10_000)
//.into(object : CustomTarget<Drawable>() {
//    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
//        // Получаем размеры изображения
//        val width = resource.intrinsicWidth
//        val height = resource.intrinsicHeight
//
//        // Получаем размеры экрана
//        val displayMetrics = binding.root.context.resources.displayMetrics
//        val screenWidth = displayMetrics.widthPixels
//
//        // Рассчитываем высоту изображения в соответствии с шириной экрана
//        val calculatedHeight = (screenWidth.toFloat() / width.toFloat() * height).toInt()
//
//        // Устанавливаем размеры в ImageView
//        binding.Attachment.layoutParams.width = screenWidth
//        binding.Attachment.layoutParams.height = calculatedHeight
//        binding.Attachment.setImageDrawable(resource)
//    }
//
//    override fun onLoadCleared(placeholder: Drawable?) {
//        // Метод вызывается, когда изображение было очищено
//    }
//})