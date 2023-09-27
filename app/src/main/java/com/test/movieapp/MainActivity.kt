package com.test.movieapp

import Genre
import Movie
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.test.movieapp.adapter.MovieAdapter
import com.test.movieapp.api.ApiService
import com.test.movieapp.databinding.ActivityMainBinding
import com.test.movieapp.dialog.BottomSheetFilterDialog
import com.test.movieapp.presenter.MainPresenter
import com.test.movieapp.util.EndlessRecyclerViewListener
import retrofit2.adapter.rxjava2.HttpException
import java.io.IOException

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter : MainPresenter
    private val apiService by lazy{
        ApiService.create()
    }

    private lateinit var adapter: MovieAdapter
    private lateinit var scrollListener: EndlessRecyclerViewListener
    private var movieList: ArrayList<Movie> = ArrayList()

    private var genreIds : String = ""
    val genreList: ArrayList<Genre> = ArrayList()
    private lateinit var filterDialog: BottomSheetFilterDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        presenter = MainPresenter(this, apiService)

        adapter = MovieAdapter(this, movieList, object : MovieAdapter.OnItemClickListener {
            override fun onItemClickListener(movieId: Int) {
                val intent = Intent(activity, MovieDetailActivity::class.java)
                intent.putExtra("movie_id", movieId)
                startActivity(intent)
            }
        })

        val gridLayoutManager = GridLayoutManager(this, 2)
        initGridLayoutManager(binding.rvMovie, adapter, gridLayoutManager)
        scrollListener = object : EndlessRecyclerViewListener(gridLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                presenter.getMovieList(page + 1, genreIds )
            }
        }

        binding.rvMovie.addOnScrollListener(scrollListener)
        binding.swipeRefresh.setOnRefreshListener {
            presenter.getMovieList(1, genreIds)
            if(binding.fab.visibility == View.GONE) {
                presenter.getGenres()
            }
        }


        filterDialog = BottomSheetFilterDialog(this, genreList, object : BottomSheetFilterDialog.OnSubmitListener {
            override fun onSubmitListener(selectedGenres: String) {
                genreIds = selectedGenres
                showLoadingDialog()
                presenter.getMovieList(1, genreIds)
            }
        })

        showLoadingDialog()
        presenter.getMovieList(1, genreIds)
        presenter.getGenres()
    }

    private fun initGridLayoutManager(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>?,
        gridLayoutManager: GridLayoutManager?
    ) {
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
    }

    fun updateView(list: ArrayList<Movie>, page: Int) {
        if (page == 1) {
            scrollListener.resetState()
            movieList.clear()
        }
        movieList.addAll(list)
        adapter.notifyDataSetChanged()

        showEmptyState(movieList.isEmpty())
        binding.swipeRefresh.isRefreshing = false
    }

    fun handleError(t: Throwable) {
        binding.swipeRefresh.isRefreshing = false
        val errorMessage = when (t) {
            is IOException -> "Network error. Please check your internet connection and try again."
            is HttpException -> "An unexpected error occurred. Please try again."
            else -> t.localizedMessage
        }

        showEmptyState(movieList.isEmpty())
        showSnackbar(binding.root, errorMessage, true)
    }

    fun showFilterButton(list: ArrayList<Genre>) {
        if(list.size > 0) {
            genreIds = ""
            genreList.clear()
            genreList.addAll(list)
            binding.fab.visibility = View.VISIBLE
            binding.fab.setOnClickListener {
                filterDialog.show(supportFragmentManager, BottomSheetFilterDialog.TAG)
            }
        } else {
            binding.fab.visibility = View.GONE
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.emptyMovie.visibility = if(isEmpty) View.VISIBLE else View.GONE
        binding.tvEmptyMovie.visibility = if(isEmpty) View.VISIBLE else View.GONE
        binding.rvMovie.visibility = if(isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
    }

    override fun onResume() {
        super.onResume()
        if(binding.fab.visibility == View.GONE) {
            presenter.getGenres()
        }
    }
}