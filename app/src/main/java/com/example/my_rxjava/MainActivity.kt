package com.example.my_rxjava

import android.os.Bundle
import android.os.Looper
import android.util.Log
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
            Log.d("rr", text.toString())

            count = 0
        }

        val flowable = subject.toFlowable(BackpressureStrategy.DROP)

        val textFlo = Flowable.fromIterable(Text.text.split(" "))

        disposable = flowable
            .subscribeOn(Schedulers.io())
            .debounce(700, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .flatMap { substring ->
                textFlo
                    .map { string ->
                        Pair(
                            substring.toLowerCase(Locale.ROOT).trim(),
                            string.toLowerCase(Locale.ROOT).trim()
                        )
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (substring, string) ->

                getMatchesCounts(string, substring)

               Log.d("dsf", "${Thread.currentThread()} ${Looper.getMainLooper().thread}")

            }, {
            })


    }

    private fun getMatchesCounts(string: String, substring: String) {
        var substringCounts: Int
        var last = 0

        do {
            substringCounts = string.indexOf(substring, last)

            if (substringCounts != -1) {
                countText.text = (++count).toString()
                Log.d("1", count.toString())
            } else {
                countText.text = count.toString()
            }

            last = substringCounts + substring.length
        } while (substringCounts != -1)
    }

    override fun onDestroy() {
        disposable?.dispose()
        disposable = null
        super.onDestroy()
    }
}