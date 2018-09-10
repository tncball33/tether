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

package com.fjordnet.sample.tether.form

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fjordnet.sample.tether.R
import com.fjordnet.sample.tether.interfaces.ContextService
import com.fjordnet.sample.tether.util.Regexes
import com.fjordnet.tether.binder.Driver
import com.fjordnet.tether.binder.toDriver
import com.fjordnet.tether.reactive.toForeverObservable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class FormViewModel(private val contextService: ContextService) : ViewModel() {

    val buttonEnabledDriver: Driver<Boolean>
    val firstNameErrorMessageDriver: Driver<String>
    val emailErrorMessageDriver: Driver<String>

    val firstNameText = MutableLiveData<String>()
    val emailText = MutableLiveData<String>()

    init {
        val firstNameValidObservable = firstNameText.toForeverObservable()
                .map {
                    !it.isEmpty()
                }

        val firstNameErrorMessage: Observable<String> = Observable.zip(
                firstNameValidObservable,
                firstNameText.toForeverObservable(),
                BiFunction<Boolean, String, Pair<Boolean, String>> { t1, t2 ->
                    t1 to t2
                })
                .map {
                    val isValid = it.first
                    val firstNameText = it.second

                    if (!isValid && !firstNameText.isEmpty()) {
                        contextService.getString(R.string.first_name_error)
                    } else {
                        ""
                    }
                }

        val emailValidObservable = emailText.toForeverObservable()
                .map {
                    Regexes.emailRegex.matches(it)
                }

        val emailErrorMessage: Observable<String> = Observable.zip(
                emailValidObservable,
                emailText.toForeverObservable(),
                BiFunction<Boolean, String, Pair<Boolean, String>> { t1, t2 ->
                    t1 to t2
                })
                .map {
                    val isValid = it.first
                    val emailText = it.second

                    if (!isValid && !emailText.isEmpty()) {
                        contextService.getString(R.string.email_error)
                    } else {
                        ""
                    }
                }

        val buttonEnabled: Observable<Boolean> =
                Observable.combineLatest(
                        firstNameValidObservable,
                        emailValidObservable,
                        BiFunction { firstNameValid, emailValid ->
                            firstNameValid && emailValid
                        })

        firstNameErrorMessageDriver = firstNameErrorMessage.toDriver()
        emailErrorMessageDriver = emailErrorMessage.toDriver()
        buttonEnabledDriver = buttonEnabled.toDriver(false)
    }
}