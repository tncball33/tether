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

package com.fjordnet.sample.tether.interfaces

import com.fjordnet.sample.tether.util.random
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class SearchServiceImpl : SearchService {

    private val searchItems = arrayListOf<String>()

    init {
        for (i in 0..100) {
            searchItems.add("Search Item $i")
        }
    }

    override fun performSearch(): Single<List<String>> {
        return resultsSingle()
                .delay(2, TimeUnit.SECONDS)
    }

    private fun resultsSingle(): Single<List<String>> {
        return Single.defer {
            val upperBound = (0..100).random()

            val results = arrayListOf<String>()

            for (i in 0..upperBound) {
                results.add(searchItems[i])
            }

            return@defer Single.just(results)
        }
    }
}