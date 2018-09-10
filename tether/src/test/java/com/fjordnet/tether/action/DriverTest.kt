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

import androidx.lifecycle.MutableLiveData
import com.fjordnet.tether.BaseTest
import com.fjordnet.tether.binder.Driver
import com.fjordnet.tether.binder.toDriver
import com.fjordnet.tether.reactive.toForeverObservable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DriverTest: BaseTest() {

    @Test
    fun testDriverDefaultValue() {
        val defaultValue = "Default Value"
        val driver = Driver(defaultValue)
        driver.observeOnScheduler = uiScheduler
        val disposable = driver.observable.subscribe { assert(it == defaultValue) }
        triggetRxSchedulers()
        disposable.dispose()
    }

    @Test
    fun testDriverFlowFromLiveDataChange() {
        val errorString = "Error!"
        val errorDriver: Driver<String>
        val testLiveData = MutableLiveData<String>()

        errorDriver = testLiveData
                .toForeverObservable()
                .map { if (it.isEmpty()) errorString else "" }
                .toDriver()
        errorDriver.observeOnScheduler = uiScheduler
        errorDriver.observable.subscribe {
            if(it.isNotEmpty()) {
                assert(it == errorString)
                return@subscribe
            }
            assert(it.isEmpty())
        }

        // Test success case
        testLiveData.postValue("A String")
        triggetRxSchedulers()

        // Test error case
        testLiveData.postValue("")
        triggetRxSchedulers()
    }

}