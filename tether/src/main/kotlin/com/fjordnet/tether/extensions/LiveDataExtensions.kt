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

package com.fjordnet.tether.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fjordnet.tether.binder.Driver
import com.fjordnet.tether.binder.impl.BindableImpl
import com.fjordnet.tether.type.Optional
import io.reactivex.Observable

/**
 * For linking a [Observable] to lifecycle callbacks with emission through [LiveData]
 */
fun <EmissionType> MutableLiveData<EmissionType>.bind(
        lifeCycleOwner: LifecycleOwner,
        source: Driver<EmissionType>
) {
    val bindable = BindableImpl<EmissionType> { value = it }
    bindable.bind(lifeCycleOwner, source)
}

/**
 * The [LiveData] uses an [Optional] so we can pass null values through Rx chains.
 *
 * Convenience function to update the backing value. This is so we don't have to keep repeating these two lines.
 * We have to set value to itself for [LiveData] to catch the change and propagate it.
 */
fun <EmissionType> MutableLiveData<Optional<EmissionType>>.updateValue(item: EmissionType?) {
    value?.item = item
    value = value
}
