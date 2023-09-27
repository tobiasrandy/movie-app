package com.test.movieapp

import MovieDetailResponse
import MovieTrailer
import MovieVideosResponse
import ReviewItem
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.test.movieapp.api.ApiService
import com.test.movieapp.databinding.ActivityMovieDetailBinding
import com.test.movieapp.presenter.MovieDetailPresenter
import com.test.movieapp.util.GlideImageLoader
import com.test.movieapp.util.convertDate
import com.test.movieapp.util.getFormattedRating
import retrofit2.adapter.rxjava2.HttpException
import java.io.IOException
import kotlin.math.ceil


class MovieDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityMovieDetailBinding
    private lateinit var presenter : MovieDetailPresenter
    private val apiService by lazy{
        ApiService.create()
    }

    private lateinit var youTubePlayer: YouTubePlayer
    private var isFullscreen = false
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isFullscreen) {
                youTubePlayer.toggleFullscreen()
            } else {
                finish()
            }
        }
    }
    private lateinit var decorView: View
    private var movieId = 0
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )
        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        movieId = intent.getIntExtra("movie_id", 0)
        presenter = MovieDetailPresenter(this, apiService)

        showLoadingDialog()
        presenter.getMovieDetail(movieId)
        presenter.getMovieVideos(movieId)
        presenter.getMovieReviews(movieId)
    }

    fun updateView(response: MovieDetailResponse) {
        GlideImageLoader(this).loadImage(response.backdropPath, binding.expandedImage, R.drawable.movie_placeholder, R.drawable.movie_placeholder)
        binding.toolbar.title = response.title
        binding.tvSynopsis.text = response.overview

        val releaseDate = if(!response.releaseDate.isNullOrEmpty()) response.releaseDate.split("-")[0] else ""
        val duration = if(response.runtime != 0) convertDuration(response.runtime!!) else ""
        binding.tvMovieInfo.text = "$releaseDate • $duration"

        if(response.voteAverage != null) {
            val score = ceil(response.voteAverage * 10).toInt()
            binding.scoreBar.progress = score
            binding.tvScore.text = "$score"
            binding.scoreContainer.visibility = View.VISIBLE
            setToolbarListener()
        }

        for (genre in response.genres!!) {
            val chip = Chip(this)
            chip.text = genre.name
            binding.chipGroup.addView(chip)
        }
    }

    fun setYoutubePlayer(response: MovieVideosResponse) {
        val trailers: List<MovieTrailer> = response.results?.filter { it.type == "Trailer" } ?: emptyList()
        if (trailers.isNotEmpty()) {
            val iFramePlayerOptions = IFramePlayerOptions.Builder()
                .controls(1)
                .fullscreen(1)
                .build()

            binding.youtubePlayerView.enableAutomaticInitialization = false
            decorView = window.decorView
            binding.youtubePlayerView.addFullscreenListener(object : FullscreenListener {
                override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                    isFullscreen = true
                    binding.youtubePlayerView.visibility = View.GONE
                    binding.fullScreenViewContainer.visibility = View.VISIBLE
                    binding.fullScreenViewContainer.addView(fullscreenView)

                    decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                        val isStatusBarVisible = visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0
                        val isNavBarVisible = visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0
                        if ((isStatusBarVisible || isNavBarVisible) && isFullscreen) {
                            handler.postDelayed({
                                hideSystemUI()
                            }, 3000)
                        }
                    }

                    hideSystemUI()
                    binding.appBarLayout.setExpanded(true)
                    setScrollingEnabled(false)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }

                override fun onExitFullscreen() {
                    isFullscreen = false
                    binding.youtubePlayerView.visibility = View.VISIBLE
                    binding.fullScreenViewContainer.visibility = View.GONE
                    binding.fullScreenViewContainer.removeAllViews()

                    handler.removeCallbacksAndMessages(null)

                    showSystemUI()
                    setScrollingEnabled(true)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            })

            binding.tvTrailer.visibility = View.VISIBLE
            binding.youtubePlayerView.visibility = View.VISIBLE
            try {
                binding.youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        this@MovieDetailActivity.youTubePlayer = youTubePlayer
                        youTubePlayer.cueVideo(trailers[0].key!!, 0f)
                    }
                }, iFramePlayerOptions)
            } catch (e: IllegalStateException) {
                binding.youtubePlayerView.visibility = View.GONE
            }

            lifecycle.addObserver(binding.youtubePlayerView)
        }
    }

    fun setReviewSection(reviewList: ArrayList<ReviewItem>) {
        if(reviewList!!.isNotEmpty()) {
            val firstReview = reviewList[0]
            binding.tvReview.visibility = View.VISIBLE
            binding.itemReview.root.visibility = View.VISIBLE

            if(reviewList.size > 1) {
                binding.btnReview.visibility = View.VISIBLE
                binding.btnReview.setOnClickListener {
                    val intent = Intent(this, UserReviewActivity::class.java)
                    intent.putExtra("movie_id", movieId)
                    startActivity(intent)
                }
            }

            if(firstReview.authorDetails!!.rating != null) {
                binding.itemReview.tvRating.text = getFormattedRating(this, firstReview.authorDetails.rating.toString())
            } else {
                binding.itemReview.star.visibility = View.GONE
            }

            binding.itemReview.tvDate.text = convertDate(firstReview.createdAt!!, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "dd MMMM yyyy")
            binding.itemReview.tvTitle.text = "A review by ${firstReview.author}"
            binding.itemReview.tvUser.text = firstReview.author
            binding.itemReview.tvReviewComment.text = firstReview.content

            binding.itemReview.root.setPadding(0, 0, 0, 0)
        }
    }

    private fun hideSystemUI() {
        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
    }

    private fun showSystemUI() {
        decorView.setOnSystemUiVisibilityChangeListener(null)
        val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
        decorView.systemUiVisibility = uiOptions
    }

    private fun setToolbarListener() {
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val scrollPercentage = -verticalOffset.toFloat() / totalScrollRange.toFloat()
            binding.scoreContainer.visibility = if (scrollPercentage == 1.0f) View.GONE else View.VISIBLE
        }
    }

    private fun setScrollingEnabled(enabled: Boolean) {
        val appBarParams = binding.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val appBarBehavior = appBarParams.behavior as AppBarLayout.Behavior
        appBarBehavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return enabled
            }
        })

        binding.scrollview.isNestedScrollingEnabled = enabled
    }

    private fun convertDuration(minutes: Int): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return if (hours > 0) {
            hours.toString() + "h " + remainingMinutes + "m"
        } else {
            "$remainingMinutes m"
        }
    }

    fun handleError(t: Throwable) {
        val errorMessage = when (t) {
            is IOException -> "Network error. Please check your internet connection and try again."
            is HttpException -> "An unexpected error occurred. Please try again."
            else -> t.localizedMessage
        }
        showSnackbar(binding.root, errorMessage, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        presenter.unsubscribe()
    }
}