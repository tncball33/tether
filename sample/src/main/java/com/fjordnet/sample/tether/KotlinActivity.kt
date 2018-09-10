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

package com.fjordnet.sample.tether

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import com.fjordnet.sample.tether.form.FormFragment
import com.fjordnet.sample.tether.search.SearchFragment
import com.fjordnet.sample.tether.slider.SliderFragment
import com.fjordnet.sample.tether.databinding.ActivityMainBinding

/**
 * Flow Controller
 */
class KotlinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(binding.toolBar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.navigationView.setNavigationItemSelectedListener {
            it.isChecked = true

            binding.drawerLayout.closeDrawers()

            when (it.itemId) {
                R.id.nav_slider -> {
                    showSliderFragment()
                }
                R.id.nav_form -> {
                    showFormFragment()
                }
                R.id.nav_search -> {
                    showSearchFragment()
                }
            }

            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item ?: return super.onOptionsItemSelected(item)

        return when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(Gravity.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSearchFragment() {
        val searchFragment = SearchFragment.newInstance()

        supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, searchFragment)
                .commit()
    }

    private fun showFormFragment() {
        val formFragment = FormFragment.newInstance()

        supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, formFragment)
                .commit()
    }

    private fun showSliderFragment() {
        val sliderFragment = SliderFragment.newInstance()

        supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, sliderFragment)
                .commit()
    }
}
