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

package com.fjordnet.tether.binder.interfaces

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.fjordnet.tether.binder.BindManager
import com.fjordnet.tether.binder.Binder
import com.fjordnet.tether.binder.Driver

/**
 * Interface for updating [LiveData] values
 */
interface Bindable<Value> {

    var bindValue: Value?

    fun bind(owner: LifecycleOwner, source: Driver<Value>) {

        val bind = Binder(owner, this, source)

        @Suppress("UNCHECKED_CAST")
        (bind as? Binder<Any>)?.let {
            BindManager.addBind(it)
        }
    }
}