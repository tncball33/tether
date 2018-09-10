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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.fjordnet.sample.tether.R
import com.fjordnet.sample.tether.interfaces.ContextServiceImpl
import com.fjordnet.sample.tether.databinding.FragmentFormBinding
import com.fjordnet.tether.extensions.bind
import com.fjordnet.tether.extensions.rxEnabled
import com.fjordnet.tether.extensions.rxErrorMessage
import com.fjordnet.tether.extensions.textChanges

class FormFragment : Fragment() {

    companion object {
        fun newInstance(): FormFragment {
            return FormFragment()
        }
    }

    private lateinit var binding: FragmentFormBinding
    private lateinit var viewModel: FormViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_form, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this, FormViewModelFactory(ContextServiceImpl(context!!)))
                .get(FormViewModel::class.java)

        bindViewModel()
    }

    private fun bindViewModel() {
        viewModel.firstNameText.bind(this, binding.firstNameEditText.textChanges)
        viewModel.emailText.bind(this, binding.emailEditText.textChanges)

        binding.callback.rxEnabled.bind(this, viewModel.buttonEnabledDriver)

        binding.firstNameInput.rxErrorMessage.bind(this, viewModel.firstNameErrorMessageDriver)
        binding.emailInput.rxErrorMessage.bind(this, viewModel.emailErrorMessageDriver)
    }

}