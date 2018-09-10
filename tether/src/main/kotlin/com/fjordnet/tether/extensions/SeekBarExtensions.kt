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

import android.widget.SeekBar
import com.fjordnet.tether.binder.Driver
import com.fjordnet.tether.binder.impl.BindableImpl
import com.fjordnet.tether.binder.interfaces.Bindable
import com.fjordnet.tether.binder.toDriver
import com.jakewharton.rxbinding2.widget.RxSeekBar

/**
 * @return [Driver] That emits progress changes.
 */
val SeekBar.progressChanged: Driver<Int>
    get() = RxSeekBar.changes(this).toDriver(0)

/**
 * @return [Bindable] That sets the SeekBar's max value
 */
val SeekBar.rxMax: Bindable<Int>
    get() = BindableImpl {
        max = it ?: 0
    }

/**
 * @return [Bindable] That sets the SeekBar's progress value
 */
val SeekBar.rxProgress: Bindable<Int>
    get() = BindableImpl {
        progress = it ?: 0
    }