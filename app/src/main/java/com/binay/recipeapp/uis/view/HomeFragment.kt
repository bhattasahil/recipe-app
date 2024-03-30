package com.binay.recipeapp.uis.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.binay.recipeapp.R
import com.binay.recipeapp.data.model.RecipeData
import com.binay.recipeapp.databinding.FragmentHomeBinding
import com.binay.recipeapp.uis.intent.DataIntent
import com.binay.recipeapp.uis.view.base.BaseFragment
import com.binay.recipeapp.uis.viewmodel.MainViewModel
import com.binay.recipeapp.uis.viewstate.DataState
import com.binay.recipeapp.util.NetworkUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : BaseFragment(), OnCategoryClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: CategoryRecyclerAdapter
    private lateinit var recipeAdapter: RecipeRecyclerAdapter

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        initViewModel()
        binding.categoryRecycler.setHasFixedSize(true)
        binding.categoryRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        adapter = CategoryRecyclerAdapter(
            requireContext(),
            resources.getStringArray(R.array.category_array),
            this
        )
        binding.categoryRecycler.adapter = adapter

        binding.recipeRecycler.setHasFixedSize(true)
        binding.recipeRecycler.layoutManager = GridLayoutManager(requireContext(), 2)

        recipeAdapter = RecipeRecyclerAdapter(requireContext(),
            object : RecipeRecyclerAdapter.RecipeClickListener {
                override fun onFavoriteChanged(
                    recipe: RecipeData,
                    isToFavorite: Boolean
                ) {
                    try {
                        changeFavoriteStatus(recipe, isToFavorite)
                    } catch (e: Exception) {
                        showError(getString(R.string.label_error_try_again))
                    }
                }

                override fun onRecipeClicked(recipe: RecipeData) {
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
        binding.recipeRecycler.adapter = recipeAdapter

        binding.refreshLayout.setOnRefreshListener {
            if (!NetworkUtil.isNetworkAvailable(requireContext())) {
                binding.refreshLayout.isRefreshing = false
                showError(getString(R.string.label_error_try_again))
            } else
                getCategoryWiseData()
        }

        fetchData("all")
    }

    private fun initViewModel() {
        lifecycleScope.launch {
            viewModel.dataState.collect {
                when (it) {
                    is DataState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is DataState.ResponseData -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recipeRecycler.visibility = View.VISIBLE
                        binding.refreshLayout.isRefreshing = false
                        binding.noInternetLayout.visibility = View.GONE

                        recipeAdapter.setRecipes(it.recipeResponseData.recipes)
                        if (it.recipeResponseData.recipes.isEmpty() && !NetworkUtil.isNetworkAvailable(
                                requireContext()
                            )
                        )
                            binding.noInternetLayout.visibility = View.VISIBLE

                    }

                    is DataState.AddToFavoriteResponse -> {
                        binding.progressBar.visibility = View.GONE
                        recipeAdapter.changeFavoriteStatus(it.recipe, it.isFromHome)
                    }

                    is DataState.Error -> {
                        showError(it.error)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    private fun fetchData(tag: String) {
        lifecycleScope.launch {
            viewModel.dataIntent.send(
                DataIntent.FetchRecipeData(
                    tag
                )
            )
        }
    }

    private fun changeFavoriteStatus(recipe: RecipeData, isToFavorite: Boolean) {
        lifecycleScope.launch {
            viewModel.dataIntent.send(
                DataIntent.ChangeFavoriteStatus(
                    recipe, isToFavorite, true
                )
            )
        }
    }

    private var currentCategoryPosition = 0
    override fun categoryClick(position: Int) {
        currentCategoryPosition = position
        adapter.updateAdapter(position)
        binding.recipeRecycler.visibility = View.GONE

        getCategoryWiseData()
    }

    private fun getCategoryWiseData() {
        binding.noInternetLayout.visibility = View.GONE
        val cuisines = resources.getStringArray(R.array.category_array)
        when (currentCategoryPosition) {
            0 -> {
                fetchData("all")
            }

            else -> {
                fetchData(cuisines[currentCategoryPosition].lowercase(Locale.ROOT))
            }
        }
    }
}