package com.sahil.recipeapp.uis.view.search

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.sahil.recipeapp.R
import com.sahil.recipeapp.data.model.SearchedRecipe
import com.sahil.recipeapp.databinding.FragmentSearchBinding
import com.sahil.recipeapp.uis.intent.DataIntent
import com.sahil.recipeapp.uis.view.RecipeDetailActivity
import com.sahil.recipeapp.uis.view.base.BaseFragment
import com.sahil.recipeapp.uis.viewmodel.MainViewModel
import com.sahil.recipeapp.uis.viewstate.DataState
import com.sahil.recipeapp.util.NetworkUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.lang.Exception

@AndroidEntryPoint
class SearchFragment : BaseFragment() {

    private lateinit var binding: FragmentSearchBinding
    private val mViewModel: MainViewModel by activityViewModels()
    private lateinit var mAdapter: SearchedRecipeAdapter

    //    Use this to search by recipes or nutrients
    private var isToSearchByRecipes = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        initViewModel()
        initSearchBy()

        binding.rvSearchRecipes.setHasFixedSize(true)
        binding.rvSearchRecipes.layoutManager = GridLayoutManager(requireContext(), 2)

        mAdapter = SearchedRecipeAdapter(requireContext(),
            object : SearchedRecipeAdapter.RecipeClickListener {
                override fun onFavoriteChanged(
                    recipe: SearchedRecipe,
                    isToFavorite: Boolean
                ) {
                    try {
                        changeFavoriteStatus(recipe, isToFavorite)
                    } catch (e: Exception) {
                        showError(e.message)
                    }
                }

                override fun onRecipeClicked(recipe: SearchedRecipe) {
                    if (!NetworkUtil.isNetworkAvailable(requireContext())) {
                        showError(getString(R.string.no_connection))
                        return
                    }
                    val intent = Intent(context, RecipeDetailActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.putExtra("recipe_id", recipe.id)
                    startActivity(intent)
                }

            })
        binding.rvSearchRecipes.adapter = mAdapter

        binding.svRecipe.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!p0.isNullOrEmpty() && p0.length >= 3) {
                        searchRecipe(p0)
                    } else {
                        mAdapter.setRecipes(ArrayList())
                    }
                }, 900)
                return false
            }
        })
    }

    private fun initSearchBy() {
        val layoutSearchFilter = binding.layoutSearchFilter
        layoutSearchFilter.cgSearchBy.setOnCheckedStateChangeListener { _, checkedId ->
            val selectedChip = checkedId.contains(layoutSearchFilter.chipRecipes.id)
            isToSearchByRecipes = selectedChip
            binding.svRecipe.setQuery("", false)
            if (isToSearchByRecipes) {
                binding.note.visibility = View.GONE
                binding.svRecipe.queryHint = getString(R.string.recipe_search_hint)
            } else {
                binding.note.visibility = View.VISIBLE
                binding.svRecipe.queryHint = getString(R.string.ingredient_search_hint)
            }
        }
    }

    private fun initViewModel() {
        lifecycleScope.launch {
            mViewModel.dataState.collect {
                when (it) {
                    is DataState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is DataState.SearchRecipes -> {
                        binding.progressBar.visibility = View.GONE
                        val recipes = it.searchRecipeData.results ?: ArrayList()
                        mAdapter.setRecipes(recipes)
                    }

                    is DataState.AddToFavoriteResponse -> {
                        binding.progressBar.visibility = View.GONE
                    }

                    is DataState.SearchRecipesByNutrients -> {
                        binding.progressBar.visibility = View.GONE
                        mAdapter.setRecipes(it.searchedRecipes)
                    }

                    is DataState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        showError(it.error)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    private fun searchRecipe(query: String) {
        lifecycleScope.launch {
            if (isToSearchByRecipes) {
                binding.svRecipe.queryHint = getString(R.string.recipe_search_hint)
                mViewModel.dataIntent.send(
                    DataIntent.SearchRecipe(
                        query
                    )
                )
            } else {
                binding.svRecipe.queryHint = getString(R.string.ingredient_search_hint)
                mViewModel.dataIntent.send(DataIntent.SearchRecipesByNutrients(query))
            }
        }
    }

    private fun changeFavoriteStatus(recipe: SearchedRecipe, isToFavorite: Boolean) {
        lifecycleScope.launch {
            mViewModel.dataIntent.send(
                DataIntent.ChangeFavoriteStatusFromSearch(
                    recipe, isToFavorite
                )
            )
        }
    }

}