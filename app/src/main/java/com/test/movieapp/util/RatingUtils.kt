package com.test.movieapp.util

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.test.movieapp.R

fun getFormattedRating(context: Context, rating: String): SpannableStringBuilder {
    val stringBuilder = SpannableStringBuilder()
    stringBuilder.append(rating)
    stringBuilder.setSpan(
        StyleSpan(Typeface.BOLD),
        stringBuilder.length - rating.length,
        stringBuilder.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    stringBuilder.setSpan(
        RelativeSizeSpan(1f),
        stringBuilder.length - rating.length,
        stringBuilder.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    val maxRating = "/10"
    stringBuilder.append(maxRating)
    stringBuilder.setSpan(
        context.resources.getColor(R.color.md_grey_300),
        stringBuilder.length - maxRating.length,
        stringBuilder.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    stringBuilder.setSpan(
        RelativeSizeSpan(0.8f),
        stringBuilder.length - maxRating.length,
        stringBuilder.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    return stringBuilder
}