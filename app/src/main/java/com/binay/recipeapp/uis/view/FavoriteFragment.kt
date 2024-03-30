package com.binay.recipeapp.uis.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.binay.recipeapp.R
import com.binay.recipeapp.data.model.RecipeData
import com.binay.recipeapp.databinding.FragmentFavoriteBinding
import com.binay.recipeapp.uis.intent.DataIntent
import com.binay.recipeapp.uis.view.base.BaseFragment
import com.binay.recipeapp.uis.viewmodel.MainViewModel
import com.binay.recipeapp.uis.viewstate.DataState
import com.binay.recipeapp.util.NetworkUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoriteFragment : BaseFragment() {

    private lateinit var mBinding: FragmentFavoriteBinding
    private lateinit var mAdapter: RecipeRecyclerAdapter
    private val mViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentFavoriteBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() {
        mAdapter = RecipeRecyclerAdapter(requireContext(),
            object : RecipeRecyclerAdapter.RecipeClickListener {
                override fun onFavoriteChanged(
                    recipe: RecipeData,
                    isToFavorite: Boolean
                ) {
                    changeFavoriteStatus(recipe, isToFavorite)
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

        mBinding.rvFavorite.adapter = mAdapter
        mBinding.rvFavorite.setHasFixedSize(true)
        mBinding.rvFavorite.layoutManager = GridLayoutManager(requireContext(), 2)

    }

    private fun initViewModel() {
        lifecycleScope.launch {
            mViewModel.dataState.collect {
                when (it) {
                    is DataState.Loading -> {

                    }

                    is DataState.AddToFavoriteResponse -> {
                        val isFavorite = it.recipe.isFavorite
                        if (!isFavorite) {
                            mAdapter.removeRecipe(it.recipe)
                        }
//                        When Favorite is added through HomeFragment
                        else {
                            mAdapter.addRecipe(it.recipe)
                        }
                    }

                    is DataState.FavoriteResponse -> {
                        val recipes = it.recipes ?: ArrayList()
                        mAdapter.setRecipes(recipes)
                    }

                    is DataState.Error -> {
                        showError(it.error)
                    }

                    else -> {

                    }
                }
            }
        }

        fetchData()
    }


    private fun changeFavoriteStatus(recipe: RecipeData, isToFavorite: Boolean) {
        lifecycleScope.launch {
            mViewModel.dataIntent.send(
                DataIntent.ChangeFavoriteStatus(
                    recipe, isToFavorite, false
                )
            )
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            mViewModel.dataIntent.send(
                DataIntent.FetchFavoriteRecipe
            )
        }
    }

}