package com.test.movieapp.presenter

import Genre
import Movie
import MovieGenresResponse
import MovieListResponse
import com.test.movieapp.MainActivity
import com.test.movieapp.api.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class MainPresenter(
    private var view: MainActivity,
    private var apiService: ApiService
) {
    private var composite : CompositeDisposable = CompositeDisposable()

    fun getMovieList(page: Int, genres: String){
        apiService.getMovieList(page, genres)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MovieListResponse> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {
                    composite.add(d)
                }

                override fun onNext(response: MovieListResponse) {
                    view.hideLoadingDialog()
                    view.updateView(response.results!! as ArrayList<Movie>, page)
                }

                override fun onError(t: Throwable) {
                    view.hideLoadingDialog()
                    view.handleError(t)
                }
            })
    }

    fun getGenres(){
        apiService.getGenres()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MovieGenresResponse> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {
                    composite.add(d)
                }

                override fun onNext(response: MovieGenresResponse) {
                    view.hideLoadingDialog()
                    view.showFilterButton(response.genres as ArrayList<Genre>)
                }

                override fun onError(t: Throwable) {
                    view.hideLoadingDialog()
                }
            })
    }

    fun unsubscribe() {
        composite.dispose()
    }
}