package com.fjordnet.tether.viewmodel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel


/**
 * Base class for [ViewModel]s to participate in the binding process.
 * Hooks into [onCleared] to notify [Binder]s when it has been destroyed.
 */
@Suppress("LeakingThis")
open class LifeCycleViewModel : ViewModel(), LifecycleOwner {

    private val lifeCycleRegistry = LifecycleRegistry(this)

    init {
        lifeCycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onCleared() {
        super.onCleared()

        lifeCycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun getLifecycle(): Lifecycle {
        return lifeCycleRegistry
    }
}