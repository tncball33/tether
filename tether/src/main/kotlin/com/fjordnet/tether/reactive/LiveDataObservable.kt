/*
 * Copyright 2017-2018 Fjord
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fjordnet.tether.reactive

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

/**
 * zhaowei
 * Created by zhaowei on 2017/5/19.
 * LiveData2Observable
 */
class LiveDataObservable<T>(private val owner: LifecycleOwner?, val data: LiveData<T>) : Observable<T>() {
    override fun subscribeActual(observer: Observer<in T>) {
        val liveDataObserver = LiveDataObserver(data, observer)
        observer.onSubscribe(liveDataObserver)
        if (owner == null) {
            data.observeForever(liveDataObserver)
        } else {
            data.observe(owner, liveDataObserver)
        }
    }

    class LiveDataObserver<T>(private val data: LiveData<T>, private val observer: Observer<in T>) : MainThreadDisposable(), androidx.lifecycle.Observer<T> {
        override fun onDispose() {
            data.removeObserver(this)
        }

        override fun onChanged(t: T?) {
            if (null != t) {
                observer.onNext(t)
            }
        }
    }
}

fun <T> LiveData<T>.toObservable(owner: LifecycleOwner): Observable<T> {
    return LiveDataObservable(owner, this)
}

fun <T> LiveData<T>.toForeverObservable(): Observable<T> {
    return LiveDataObservable(null, this)
}