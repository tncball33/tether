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

package com.fjordnet.sample.tether.slider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.fjordnet.sample.tether.R
import com.fjordnet.sample.tether.databinding.FragmentSliderBinding
import com.fjordnet.tether.extensions.bind
import com.fjordnet.tether.extensions.progressChanged
import com.fjordnet.tether.extensions.rxText

class SliderFragment : Fragment() {

    companion object {
        fun newInstance(): SliderFragment {
            return SliderFragment()
        }
    }

    lateinit var viewModel: SliderViewModel

    private lateinit var binding: FragmentSliderBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.fragment_slider, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(SliderViewModel::class.java)

        bindViewModel()
    }

    private fun bindViewModel() {
        viewModel.sliderProgress.bind(this, binding.seekBar.progressChanged)

        binding.valueText.rxText.bind(this, viewModel.textDriver)
    }
}