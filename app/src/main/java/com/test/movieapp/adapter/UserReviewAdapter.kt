package com.test.movieapp.adapter

import ReviewItem
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.test.movieapp.databinding.ItemReviewBinding
import com.test.movieapp.util.convertDate
import com.test.movieapp.util.getFormattedRating

class UserReviewAdapter(private val context: Context, private val reviewList: ArrayList<ReviewItem>) :
    RecyclerView.Adapter<UserReviewAdapter.UserReviewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReviewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserReviewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    override fun onBindViewHolder(holder: UserReviewHolder, position: Int) {
        val item = reviewList[position]
        holder.bind(item)
    }

    inner class UserReviewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReviewItem) {
            if(item.authorDetails!!.rating != null) {
                binding.star.visibility = View.VISIBLE
                binding.tvRating.visibility = View.VISIBLE
                binding.tvRating.text = getFormattedRating(context, item.authorDetails.rating.toString())
            } else {
                binding.star.visibility = View.GONE
                binding.tvRating.visibility = View.GONE
            }
            binding.tvDate.text = convertDate(item.createdAt!!, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "dd MMMM yyyy")
            binding.tvTitle.text = "A review by ${item.author}"
            binding.tvUser.text = item.author
            binding.tvReviewComment.text = item.content
        }
    }
}