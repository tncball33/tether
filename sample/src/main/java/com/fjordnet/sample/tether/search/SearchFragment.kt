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

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fjordnet.sample.tether.interfaces.SearchServiceImpl
import com.fjordnet.sample.tether.R
import com.fjordnet.sample.tether.databinding.FragmentSearchBinding
import com.fjordnet.tether.binder.toDriver
import com.fjordnet.tether.extensions.bind
import com.fjordnet.tether.extensions.queryChanges
import com.fjordnet.tether.extensions.rxNotifyDataSetChanged
import com.fjordnet.tether.extensions.rxVisibility
import java.util.concurrent.TimeUnit

class SearchFragment : Fragment() {

    companion object {
        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }

    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: SearchViewModel

    private var isSearchViewBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_search, container, false)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.search_menu, menu)

        menu?.let {
            val searchItem = it.findItem(R.id.action_search)
            searchItem.expandActionView()

            val searchView = searchItem.actionView as SearchView
            searchView.maxWidth = Int.MAX_VALUE
            searchView.setIconifiedByDefault(false)
            searchView.queryHint = getString(R.string.search)

            bindSearchView(searchView)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this, SearchViewModelFactory(this, SearchServiceImpl()))
                .get(SearchViewModel::class.java)

        setupRecyclerView()
        bindViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = SearchAdapter(viewModel.searchResults)
    }

    private fun bindViewModel() {
        viewModel.searchResults.bind(this, viewModel.searchResultsDriver)

        val newResultDriver = viewModel.searchResultsDriver.rawObservable
                .map { Unit }
                .toDriver(Unit)

        binding.recyclerView.rxVisibility.bind(this, viewModel.recyclerViewVisibilityDriver)
        binding.recyclerView.rxNotifyDataSetChanged.bind(this, newResultDriver)

        binding.progressBar.rxVisibility.bind(this, viewModel.progressBarVisibilityDriver)
    }

    private fun bindSearchView(searchView: SearchView) {
        if (isSearchViewBound) {
            return
        }

        isSearchViewBound = true

        val queryDriver = searchView.queryChanges.rawObservable
                .debounce(500, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .toDriver()

        viewModel.searchText.bind(this, queryDriver)
    }
}