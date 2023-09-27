package com.test.movieapp.dialog

import Genre
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.test.movieapp.R
import com.test.movieapp.databinding.BottomSheetFilterDialogLayoutBinding

class BottomSheetFilterDialog(private var mContext: Context, private val genreList: ArrayList<Genre>, private val listener: OnSubmitListener) : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetFilterDialogLayoutBinding
    private val temporaryList: ArrayList<Genre> = ArrayList()

    interface OnSubmitListener {
        fun onSubmitListener(selectedGenres: String)
    }

    @SuppressLint("UseCompatLoadingForColorStateLists", "PrivateResource")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetFilterDialogLayoutBinding.inflate(layoutInflater, container, false)

        temporaryList.clear()
        temporaryList.addAll(genreList.map { it.copy() })

        for (genre in temporaryList) {
            val chip = Chip(mContext)
            chip.text = genre.name
            chip.isCheckable = true
            chip.chipCornerRadius = 40F

            chip.isChecked = genre.isChecked
            chip.chipBackgroundColor = mContext.resources.getColorStateList(if (chip.isChecked) R.color.purple_500 else com.google.android.material.R.color.mtrl_btn_transparent_bg_color)
            chip.chipStrokeColor = mContext.resources.getColorStateList(if (chip.isChecked) R.color.white else R.color.md_grey_400)
            chip.setTextColor(mContext.resources.getColor(if (chip.isChecked) R.color.white else R.color.md_grey_400))

            chip.setOnCheckedChangeListener { _, isChecked ->
                chip.chipBackgroundColor = mContext.resources.getColorStateList(if (isChecked) R.color.purple_500 else com.google.android.material.R.color.mtrl_btn_transparent_bg_color)
                chip.chipStrokeColor = mContext.resources.getColorStateList(if (isChecked) R.color.white else R.color.md_grey_400)
                chip.setTextColor(mContext.resources.getColor(if (isChecked) R.color.white else R.color.md_grey_400))
                genre.isChecked = isChecked
            }

            binding.chipGroupGenres.addView(chip)
        }

        binding.btnClearFilter.setOnClickListener {
            for (i in 0 until binding.chipGroupGenres.childCount) {
                val chip = binding.chipGroupGenres.getChildAt(i) as Chip
                chip.isChecked = false
            }
        }

        binding.btnApply.setOnClickListener {
            genreList.clear()
            genreList.addAll(temporaryList)
            listener.onSubmitListener(getSelectedGenreIds())
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    fun getSelectedGenreIds(): String {
        val selectedGenres = genreList
            .filter { it.isChecked }
            .map { it.id }
            .joinToString(",")
        return selectedGenres
    }

    companion object {
        const val TAG = "BottomSheetFilterDialog"
    }
}