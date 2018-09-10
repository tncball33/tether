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

import androidx.annotation.VisibleForTesting
import com.fjordnet.tether.binder.interfaces.Source
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Represents a contract for binding that guarantees delivery on the main thread as well as never failing.
 * This contract is needed to remove hard to debug errors where the user could bind to a source that emits
 * on a separate thread.
 * @param decoratedObservable Underlying observable that the driver decorates to conform to the above contract.
 * @param defaultValue Value emitted if the [decoratedObservable] errors.
 */
class Driver<EmissionType>(
        private val decoratedObservable: Observable<EmissionType>?,
        private val defaultValue: EmissionType
) : Source<EmissionType> {

    constructor(defaultValue: EmissionType) : this(null, defaultValue)

    @VisibleForTesting
    var observeOnScheduler: Scheduler? = null

    override val observable: Observable<EmissionType>
        get() {
            return if (decoratedObservable != null && defaultValue != null) {
                decoratedObservable
                        .onErrorReturn { defaultValue }
                        .observeOn(observeOnScheduler ?: AndroidSchedulers.mainThread())
            } else {
                Observable.just(defaultValue)
                        .observeOn(observeOnScheduler ?: AndroidSchedulers.mainThread())
            }
        }

    /**
     * @return The underlying [Observable] but without Driver semantics.
     * Which means this observable doesn't guarantee delivery on the main thread.
     * Use this when adding new functionality to the [Driver] is needed.
     */
    val rawObservable: Observable<EmissionType>
        get() {
            return if (decoratedObservable != null && defaultValue != null) {
                return decoratedObservable
            } else {
                Observable.just(defaultValue)
            }
        }
}

/**
 * Extension method for converting [Observable] to [Driver] that emits empty string
 */
fun Observable<String>.toDriver(): Driver<String> {
    return toDriver("")
}

/**
 * Extension method for converting [Observable] to a [Driver]
 * @param onErrorJustReturn default value to emit if the observable emits an error.
 */
fun <EmissionType> Observable<EmissionType>.toDriver(onErrorJustReturn: EmissionType): Driver<EmissionType> {
    return Driver(this, onErrorJustReturn)
}