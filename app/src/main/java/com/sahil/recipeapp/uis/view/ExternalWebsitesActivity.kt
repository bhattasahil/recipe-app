package com.sahil.recipeapp.uis.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.sahil.recipeapp.R
import com.sahil.recipeapp.data.model.WebsiteData
import com.sahil.recipeapp.databinding.ActivityExternalWebsitesBinding
import com.sahil.recipeapp.uis.intent.DataIntent
import com.sahil.recipeapp.uis.viewmodel.MainViewModel
import com.sahil.recipeapp.uis.viewstate.DataState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExternalWebsitesActivity : AppCompatActivity(),
    WebsiteRecyclerAdapter.OnWebsiteClickListener {

    private lateinit var mBinding: ActivityExternalWebsitesBinding
    private lateinit var mAdapter: WebsiteRecyclerAdapter
    private val mViewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityExternalWebsitesBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initView()
        initViewModel()
    }

    private fun initView() {

        mBinding.toolbar.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        mBinding.toolbar.toolbarTitle.text = getString(R.string.recipe_site)

        mBinding.recyclerView.setHasFixedSize(true)
        mBinding.recyclerView.layoutManager = GridLayoutManager(this, 2)

        mAdapter = WebsiteRecyclerAdapter(this, this)
        mBinding.recyclerView.adapter = mAdapter
    }

    private fun initViewModel() {
        lifecycleScope.launch {
            mViewModel.dataState.collect {
                when (it) {
                    is DataState.Loading -> {

                    }

                    is DataState.FetchWebsiteList -> {
                        val websiteList = it.websites
                        Log.e("list", "initViewModel: " + websiteList.size)
                        mAdapter.setData(websiteList)
                    }

                    else -> {

                    }
                }
            }
        }

        fetchData()
    }

    private fun fetchData() {
        lifecycleScope.launch {
            mViewModel.dataIntent.send(
                DataIntent.FetchWebsiteList
            )
        }
    }

    override fun onWebsiteClicked(website: WebsiteData) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("name", website.name)
        intent.putExtra("url", website.link)
        startActivity(intent)
    }
}