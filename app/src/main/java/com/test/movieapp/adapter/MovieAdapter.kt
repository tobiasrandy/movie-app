package com.test.movieapp.adapter

import Movie
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.test.movieapp.R
import com.test.movieapp.databinding.ItemMovieBinding
import com.test.movieapp.util.GlideImageLoader
import com.test.movieapp.util.convertDate
import kotlin.math.ceil

class MovieAdapter(private val context: Context, private val movieList: ArrayList<Movie>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<MovieAdapter.MovieHolder>() {

    interface OnItemClickListener {
        fun onItemClickListener(movieId: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieHolder(binding)
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    override fun onBindViewHolder(holder: MovieHolder, position: Int) {
        val item = movieList[position]
        holder.bind(item)
    }

    inner class MovieHolder(private val binding: ItemMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Movie) {
            GlideImageLoader(context).loadImage(item.posterPath, binding.moviePoster, R.drawable.movie_placeholder, R.drawable.movie_placeholder)

            if(item.voteAverage != null) {
                val score = ceil(item.voteAverage * 10).toInt()
                binding.scoreBar.progress = score
                binding.tvScore.text = "$score"
                binding.scoreContainer.visibility = View.VISIBLE
            }

            binding.tvMovieTitle.text = item.title
            binding.tvDate.text = convertDate(item.releaseDate!!, "yyyy-MM-dd", "MMM d, yyyy")

            binding.cardView.setOnClickListener {
                listener.onItemClickListener(item.id!!)
            }
        }
    }
}