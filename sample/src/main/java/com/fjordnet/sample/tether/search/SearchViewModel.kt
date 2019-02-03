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

package com.fjordnet.sample.tether.search

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fjordnet.sample.tether.interfaces.SearchService
import com.fjordnet.tether.action.Action
import com.fjordnet.tether.binder.Driver
import com.fjordnet.tether.binder.toDriver
import com.fjordnet.tether.extensions.skipNull
import com.fjordnet.tether.reactive.toForeverObservable
import com.fjordnet.tether.type.Visibility
import com.fjordnet.tether.viewmodel.LifeCycleViewModel
import io.reactivex.schedulers.Schedulers

class SearchViewModel(
        private val searchService: SearchService
) : LifeCycleViewModel() {

    val progressBarVisibilityDriver: Driver<Visibility>
    val recyclerViewVisibilityDriver: Driver<Visibility>
    val searchResultsDriver: Driver<List<String>>

    val searchText = MutableLiveData<String>()
    val searchResults = MutableLiveData<List<String>>()

    val searchAction: Action<String, List<String>> by lazy {
        Action(this, trigger = searchText.toForeverObservable().toDriver()) {
            val searchQuery = it

            if (searchQuery.isEmpty()) {
                Driver(listOf())
            } else {
                searchService.performSearch()
                        .subscribeOn(Schedulers.io())
                        .toObservable()
                        .toDriver(listOf())
            }
        }
    }

    init {
        searchResultsDriver = searchAction.toForeverObservable()
                .skipNull()
                .toDriver(listOf())

        progressBarVisibilityDriver = searchAction.executing
                .map { executing ->
                    if (executing) Visibility.VISIBLE else Visibility.INVISIBLE
                }
                .toDriver(Visibility.INVISIBLE)

        recyclerViewVisibilityDriver = searchAction.executing
                .map { executing ->
                    if (executing) Visibility.INVISIBLE else Visibility.VISIBLE
                }
                .toDriver(Visibility.INVISIBLE)
    }
}