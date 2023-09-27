package com.test.movieapp

import ReviewItem
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.test.movieapp.adapter.UserReviewAdapter
import com.test.movieapp.api.ApiService
import com.test.movieapp.databinding.ActivityUserReviewBinding
import com.test.movieapp.presenter.UserReviewPresenter
import com.test.movieapp.util.EndlessRecyclerViewListener
import retrofit2.adapter.rxjava2.HttpException
import java.io.IOException

class UserReviewActivity : BaseActivity() {
    private lateinit var binding: ActivityUserReviewBinding
    private lateinit var presenter : UserReviewPresenter
    private val apiService by lazy{
        ApiService.create()
    }

    private lateinit var adapter: UserReviewAdapter
    private lateinit var scrollListener: EndlessRecyclerViewListener
    private var reviewList: ArrayList<ReviewItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val movieId = intent.getIntExtra("movie_id", 0)
        presenter = UserReviewPresenter(this, apiService, movieId)

        adapter = UserReviewAdapter(this, reviewList)
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        initVLinearLayoutManager(binding.rvReview, adapter, linearLayoutManager)
        scrollListener = object : EndlessRecyclerViewListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                presenter.getMovieReviews(page + 1 )
            }
        }
        binding.rvReview.addOnScrollListener(scrollListener)
        binding.swipeRefresh.setOnRefreshListener { presenter.getMovieReviews(1) }

        showLoadingDialog()
        presenter.getMovieReviews(1)
    }

    private fun initVLinearLayoutManager(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>?,
        linearLayoutManager: LinearLayoutManager?
    ) {
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
    }

    fun updateView(list: ArrayList<ReviewItem>, page: Int) {
        if (page == 1) {
            scrollListener.resetState()
            reviewList.clear()
        }
        reviewList.addAll(list)
        adapter.notifyDataSetChanged()

        showEmptyState(reviewList.isEmpty())
        binding.swipeRefresh.isRefreshing = false
    }

    fun handleError(t: Throwable) {
        binding.swipeRefresh.isRefreshing = false
        val errorMessage = when (t) {
            is IOException -> "Network error. Please check your internet connection and try again."
            is HttpException -> "An unexpected error occurred. Please try again."
            else -> t.localizedMessage
        }

        showEmptyState(reviewList.isEmpty())
        showSnackbar(binding.root, errorMessage, true)
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.emptyReview.visibility = if(isEmpty) View.VISIBLE else View.GONE
        binding.tvEmptyReview.visibility = if(isEmpty) View.VISIBLE else View.GONE
        binding.rvReview.visibility = if(isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
    }
}