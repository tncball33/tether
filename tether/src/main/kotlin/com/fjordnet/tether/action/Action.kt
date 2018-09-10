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

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import com.fjordnet.tether.binder.Driver
import com.fjordnet.tether.extensions.updateValue
import com.fjordnet.tether.type.Optional
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 *
 * Typical usage flow for this class:
 *
 * - Setup callbacks through [observe] or directly with LiveData observe method off [Action, error, etc][observe]
 * - Attach the input value with [input]
 * - Begin the action with [start]
 *
This class encapsulates the subscribing and monitoring of an underlying
observable. It exposes convenience LiveData objects that allow the user to monitor
meaningful events throughout the execution of said observable. The goal of this is to make
the language stronger around what is happening throughout the lifetime of the observable.
The Action itself is also a LiveData object and is the hook into observing the data emitted from the
underlying observable.


If you need a short lived operation, we have to null out the values in the internal live data so that
observing again will no emit a value.  This happens when sharing view models across fragments.

To use this with RxJava we had to create a class that contains a nullable type.
This is because RxJava assumes everything to be non-null but LiveData can hold null values. I hate that we're going
to have to check against this but until LiveData and RxJava play nice together we have to.
 */
class Action<Input, Output> constructor(
        private val execute: ((Input) -> Observable<Output>)?
) : LiveData<Optional<Output>>(), LifecycleObserver {

    /**
     * This emits whenever the underlying observable has completed.
     */
    val completed: LiveData<Optional<Unit>> by lazy {
        _completed.value = Optional()
        _completed
    }

    /**
     * This emits a throwable whenever the underlying observable emits an error.
     */
    val error: LiveData<Optional<Throwable>> by lazy {
        _error.value = Optional()
        _error
    }

    /**
     * This is true when the observable is subscribed to and false once the observable has emitted data.
     */
    val executing: LiveData<Optional<Boolean>> by lazy {
        _executing.value = Optional()
        _executing
    }

    private var trigger: Driver<Input>? = null
    private var triggerExecute: ((Input) -> Driver<Output>)? = null
    private var lifeCycleOwner: LifecycleOwner? = null

    private var isUsingTrigger = false

    private val _completed = MutableLiveData<Optional<Unit>>()
    private val _error = MutableLiveData<Optional<Throwable>>()
    private val _executing = MutableLiveData<Optional<Boolean>>()

    private var observable: Observable<Output>? = null
    private var disposable: Disposable? = null

    @VisibleForTesting
    var observeOnScheduler: Scheduler? = null

    /**
     * Use this constructor to make the action stay subscribed to it's underlying observable.
     * Default behavior is the Action will dispose after the first successful emission.
     * @param lifeCycleOwner the Action will observes this to know when to dispose of the trigger subscription
     *
     */
    constructor(
            lifeCycleOwner: LifecycleOwner,
            trigger: Driver<Input>,
            triggerExecute: (Input) -> Driver<Output>
    ) : this(null) {
        this.trigger = trigger
        this.lifeCycleOwner = lifeCycleOwner
        this.triggerExecute = triggerExecute

        isUsingTrigger = true

        commonInit()
    }

    init {
        commonInit()
    }

    override fun onInactive() {
        super.onInactive()
        if (!isUsingTrigger) {
            finished()
            _completed.updateValue(null)
        }
    }

    /**
     * Optional method to simplify observing on the action.
     * A developer now doesn't have to worry about calling start prior to observing to events.
     * @param owner [LifecycleOwner] that will be used to call [LiveData.observe]
     * @param executing Lambda to pass when the call is executing.
     *          Similar to [Observable.doOnSubscribe] and [Observable.doOnComplete].
     * @param output Lambda to pass the output of the call.
     *          Similar to Rx subscribe onNext Consumer.
     * @param error Lambda to pass the error of the call.
     *          Similar to Rx subscribe onError Consumer.
     * @param completed Lambda to pass that the call has completed.
     *          Similar to Rx subscribe onComplete Action.
     */
    fun observe(
            owner: LifecycleOwner,
            executing: (executing: Boolean?) -> Unit = { },
            output: (output: Output?) -> Unit = { },
            error: (error: Throwable?) -> Unit = { },
            completed: () -> Unit = { }
    ): Action<Input, Output> {

        observe(owner,
                Observer { value -> output(value?.item) })
        _error.observe(owner,
                Observer { value -> error(value?.item) })
        _executing.observe(owner,
                Observer { value -> executing(value?.item) })
        _completed.observe(owner,
                Observer { completed() })

        return this
    }

    /**
     * Add your [Input] data to create the underlying observable
     * @return [Action] to chain calls
     */
    fun input(input: Input): Action<Input, Output> {
        observable = execute?.invoke(input)

        return this
    }

    /**
     * Begins the flow and subscribes to the underlying [Observable].  State updates will be passed
     * through the [LiveData] variables that are being observed.
     */
    fun start() {
        val isExecuting = _executing.value?.item == true
        if (isExecuting) {
            return
        }

        // If the input was not set, error out
        if (observable == null) {
            _error.updateValue(Throwable("Must call input(input)"))
            finished()
            return
        }

        // Inform that the observable is now executing
        _executing.updateValue(true)

        // Subscribe to the observable
        disposable = observable
                ?.observeOn(observeOnScheduler ?: AndroidSchedulers.mainThread())
                ?.subscribe({ value ->
                    this.value?.item = value
                    setValue(this.value)
                }, { error ->
                    this._error.updateValue(error)
                    finished()
                }, {
                    finished()
                    _completed.updateValue(Unit)
                    _completed.updateValue(null)
                })
    }

    private fun commonInit() {
        value = Optional()
        _executing.value = Optional()
        _completed.value = Optional()
        _error.value = Optional()

        if (isUsingTrigger) {
            lifeCycleOwner?.lifecycle?.addObserver(this)
            subscribeToTrigger()
        }
    }

    /**
     * Subscribes to a trigger [Observable] and executes the wrapped [Observable] once the trigger emits.
     * This method is used when the user wants an [Action] to be long lasting instead of transactional.
     */
    private fun subscribeToTrigger() {
        val localTrigger = trigger?.observable ?: return
        val localTriggerExecute = triggerExecute ?: return

        disposable = localTrigger
                .doOnNext {
                    _executing.updateValue(true)
                }
                .switchMap {
                    localTriggerExecute(it).observable
                }
                .subscribe { value ->
                    _executing.updateValue(false)
                    this.value?.item = value
                    setValue(this.value)
                }
    }

    private fun dispose() {
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
    }

    /**
     * Used to update values for [LiveData] where we cannot leave their value as stale
     */
    private fun finished() {
        _executing.updateValue(false)
        _error.updateValue(null)
        dispose()
        _executing.updateValue(null)
    }

    //Lifecycle
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroyed() {
        finished()
    }
}