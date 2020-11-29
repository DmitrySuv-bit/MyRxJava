package com.example.my_rxjava

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val search: TextView
        get() = findViewById(R.id.searchView)

    private val text: TextView
        get() = findViewById(R.id.text)

    private val countText: TextView
        get() = findViewById(R.id.countText)

    private var count = 0

    private val subject = PublishSubject.create<String>()

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        text.text = Text.text

        search.doOnTextChanged { text, _, _, _ ->

            subject.onNext(text.toString())
        }

        val searchObservable = subject.toFlowable(BackpressureStrategy.DROP)

        val textObservable = Flowable.fromIterable(text.text.split(" ")).delay(200L, TimeUnit.MILLISECONDS)

        disposable = searchObservable
            .subscribeOn(Schedulers.io())
            .debounce(700L, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .filter { it.isNotEmpty()}
            .flatMap { substring ->
                count = 0
                textObservable
                    .map { string ->
                        getMatchesCounts(string, substring)
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                countText.text = it.toString()
            }, {
            })
    }

    private fun getMatchesCounts(string: String, substring: String): Int {
        val stringToLowerCase = string.toLowerCase(Locale.ROOT).trim()
        val substringToLowerCase = substring.toLowerCase(Locale.ROOT).trim()

        var substringCounts: Int
        var last = 0

        do {
            substringCounts = stringToLowerCase.indexOf(substringToLowerCase, last)

            if (substringCounts != -1) {
                ++count
            }

            last = substringCounts + substringToLowerCase.length
        } while (substringCounts != -1)

        return count
    }

    override fun onDestroy() {
        disposable?.dispose()
        disposable = null
        super.onDestroy()
    }
}