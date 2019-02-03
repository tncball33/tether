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

package com.fjordnet.tether.binder

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.fjordnet.tether.binder.interfaces.Bindable
import com.fjordnet.tether.binder.interfaces.Source
import io.reactivex.disposables.Disposable

/**
 * Binds to a [LifecycleOwner] by subscribing to [Source.observable] and updating [Bindable.bindValue].
 *
 * Handles disposing the subscription from [Lifecycle.Event.ON_PAUSE] or [Lifecycle.Event.ON_DESTROY]
 * while also resuming the subscription if necessary from [Lifecycle.Event.ON_START] or [Lifecycle.Event.ON_RESUME].
 *
 * This is a one directional bind between the [Source.observable] and [Bindable.bindValue]
 */
internal class Binder<Value>(
    private var owner: LifecycleOwner,
    private var bindable: Bindable<Value>,
    private var source: Source<Value>
) : LifecycleObserver {

    private var disposable: Disposable? = null

    /**
     * Set by the [BindManager] for clean up
     */
    var destroyed: (() -> Unit)? = null

    init {
        listen()
    }


    /**
     * Adds the [Binder] as an observer to the [LifecycleOwner] and subscribes to changes
     */
    private fun listen() {
        owner.lifecycle.addObserver(this)

        subscribe()
    }

    /**
     * Binds the [owner] and [source]
     * @param owner the [LifecycleOwner] that this should be tied to
     * @param bindable the [Bindable] that holds the type
     * @param source the [Source] which provides an observable
     */
    private fun subscribe() {
        disposable = source.observable.subscribe({
            bindable.bindValue = it
        }, {
            cleanUp()
        }, { cleanUp() })
    }

    private fun cleanUp() {
        if (disposable != null) {
            disposable?.dispose()
        }

        destroyed?.invoke()

        owner.lifecycle.removeObserver(this)
    }

    private fun resumeSubscription() {
        if (disposable == null) {
            subscribe()
        }
    }

    private fun destroySubscription() {
        if (disposable != null) {
            disposable?.dispose()
            disposable = null
        }

        cleanUp()
    }

    /**
     * Automatically called, do not call explicitly
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onLifecycleOnStart() {
        resumeSubscription()
    }

    /**
     * Automatically called, do not call explicitly
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onLifecycleOnResume() {
        resumeSubscription()
    }

    /**
     * Automatically called, do not call explicitly
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onLifecycleOnDestroy() {
        destroySubscription()
    }
}