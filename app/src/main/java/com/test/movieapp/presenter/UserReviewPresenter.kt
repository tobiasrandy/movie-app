package com.test.movieapp.presenter

import MovieReviewsResponse
import ReviewItem
import com.test.movieapp.UserReviewActivity
import com.test.movieapp.api.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class UserReviewPresenter(
    private var view: UserReviewActivity,
    private var apiService: ApiService,
    private var movideId: Int
) {
    private var composite : CompositeDisposable = CompositeDisposable()

    fun getMovieReviews(page: Int){
        apiService.getMovieReviewList(movideId, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MovieReviewsResponse> {
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable) {
                    composite.add(d)
                }

                override fun onNext(response: MovieReviewsResponse) {
                    view.hideLoadingDialog()
                    view.updateView(response.results!! as ArrayList<ReviewItem>, page)
                }

                override fun onError(t: Throwable) {
                    view.hideLoadingDialog()
                    view.handleError(t)
                }
            })
    }

    fun unsubscribe() {
        composite.dispose()
    }
}