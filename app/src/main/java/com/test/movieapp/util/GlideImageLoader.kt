package com.test.movieapp.util

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class GlideImageLoader(private val context: Context) {

    fun loadImage(
        imageUrl: String?,
        imageView: ImageView,
        placeholderResId: Int,
        errorResId: Int
    ) {
        val fullImageUrl = IMAGE_PATH + imageUrl
        Glide.with(context)
            .load(fullImageUrl)
            .apply(
                RequestOptions()
                .placeholder(placeholderResId)
                .error(errorResId)
                .diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(imageView)
    }

    companion object {
        private const val IMAGE_PATH = "https://image.tmdb.org/t/p/w500"
    }
}