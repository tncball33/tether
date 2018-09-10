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

package com.fjordnet.sample.tether.slider

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fjordnet.tether.binder.Driver
import com.fjordnet.tether.binder.toDriver
import com.fjordnet.tether.reactive.toForeverObservable

class SliderViewModel : ViewModel() {

    val textDriver: Driver<String>

    val sliderProgress = MutableLiveData<Int>()

    init {
        textDriver = sliderProgress.toForeverObservable()
                .map {
                    "$it"
                }
                .toDriver()
    }
}