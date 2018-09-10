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

import android.view.View
import com.fjordnet.tether.binder.impl.BindableImpl
import com.fjordnet.tether.binder.interfaces.Bindable
import com.fjordnet.tether.type.Visibility

/**
 * @return [Bindable] That represents the enabled state of a view.
 */
val View.rxEnabled: Bindable<Boolean>
    get() = BindableImpl {
        isEnabled = it ?: false
    }

/**
 * @return [Bindable] That represents the visibility state for a view.
 * Takes the Tether Visibility enum class so it's type safe.
 */
val View.rxVisibility: Bindable<Visibility>
    get() = BindableImpl {
        visibility = it?.visibilityInt ?: View.INVISIBLE
    }