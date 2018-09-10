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

import com.fjordnet.tether.binder.impl.SourceImpl
import com.fjordnet.tether.binder.interfaces.Source
import com.fjordnet.tether.type.Optional
import io.reactivex.Observable

/**
 * Converts an [Observable] to a [Source] for binding.
 */
fun <EmissionType> Observable<EmissionType>.toSource(): Source<EmissionType> {
    return SourceImpl {
        this
    }
}

/**
 * Extension method for ignoring a null value and unwrapping it if non-null.
 */
fun <EmissionType> Observable<Optional<EmissionType>>.skipNull(): Observable<EmissionType> {
    return filter {
        it.item != null
    }.map {
        it.item!!
    }
}