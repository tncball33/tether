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

package com.fjordnet.tether.action

import com.fjordnet.tether.BaseTest
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ActionTest: BaseTest() {

    private lateinit var action: Action<Any, Any>
    private var executingCalls = 0
    private var completedCalls = 0

    @Before
    fun setup() {
        executingCalls = 0
        completedCalls = 0

        action = Action { Observable.just(Any()) }
        action.observeForever { }
        action.executing.observeForever { value -> value.item?.let { executingCalls++ } }
        action.completed.observeForever { value -> value.item?.let { completedCalls++ } }
        action.error.observeForever { }

        action.observeOnScheduler = uiScheduler
    }

    @Test
    fun testActionCallbacks() {

        assert(action.executing.value?.item == null)

        action.input(Any())
        action.start()
        assert(action.executing.value?.item == true)

        triggetRxSchedulers()
        assert(executingCalls == 2)
        assert(completedCalls == 1)

        assert(action.error.value?.item == null)

        assert(action.value?.item != null)
    }

}