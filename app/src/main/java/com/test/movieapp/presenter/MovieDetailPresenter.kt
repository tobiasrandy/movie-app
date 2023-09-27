package com.test.movieapp.presenter

import MovieDetailResponse
import MovieReviewsResponse
import MovieVideosResponse
import ReviewItem
import com.test.movieapp.MovieDetailActivity
import com.test.movieapp.api.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class MovieDetailPresenter(
    private var view: MovieDetailActivity,
    private var apiService: ApiService
) {
    private var composite : CompositeDisposable = CompositeDisposable()

    fun getMovieDetail(id : Int){
        apiService.getMovieDetail(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<MovieDetailResponse> {
                    override fun onComplete() {

                    }

                    override fun onSubscribe(d: Disposable) {
                        composite.add(d)
                    }

                    override fun onNext(response: MovieDetailResponse) {
                        view.hideLoadingDialog()
                        view.updateView(response)
                    }

                    override fun onError(t: Throwable) {
                        view.hideLoadingDialog()
                        view.handleError(t)
                    }
                })
    }

    fun getMovieVideos(id : Int){
        apiService.getMovieVideos(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MovieVideosResponse> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {
                    composite.add(d)
                }

                override fun onNext(response: MovieVideosResponse) {
                    view.setYoutubePlayer(response)
                }

                override fun onError(t: Throwable) {
                    view.handleError(t)
                }
            })
    }

    fun getMovieReviews(id : Int){
        apiService.getMovieReviewList(id, 1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MovieReviewsResponse> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {
                    composite.add(d)
                }

                override fun onNext(response: MovieReviewsResponse) {
                    view.setReviewSection(response.results!! as ArrayList<ReviewItem>)
                }

                override fun onError(t: Throwable) {
                    view.handleError(t)
                }
            })
    }

    fun unsubscribe() {
        composite.dispose()
    }
}