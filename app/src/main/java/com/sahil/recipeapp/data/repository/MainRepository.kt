package com.sahil.recipeapp.data.repository

import android.content.Context
import com.sahil.recipeapp.data.api.ApiHelper
import com.sahil.recipeapp.data.repository.local.LocalRepo
import com.sahil.recipeapp.data.model.RecipeResponseData
import com.sahil.recipeapp.data.repository.remote.RemoteRepo
import com.sahil.recipeapp.util.NetworkUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MainRepository @Inject constructor(
    @ApplicationContext private val mContext: Context,
    private val apiHelper: ApiHelper,
    private val mLocalRepo: LocalRepo,
    private val mRemoteRepo: RemoteRepo
) {

    suspend fun getRecipes(tag: String): RecipeResponseData {
        val newTag = if (tag == "all") {
            ""
        } else {
            tag
        }
        if (!NetworkUtil.isNetworkAvailable(mContext)) {
            val recipesData = mLocalRepo.getRecipesByTag(newTag)
            if (recipesData != null) return recipesData
        }
        return mRemoteRepo.getRecipes(newTag)
    }

    suspend fun getRecipeDetail(id: Int) = apiHelper.getRecipeDetail(id)

    suspend fun searchRecipes(query: String) = apiHelper.searchRecipes(query)

    suspend fun searchRecipesByIngredients(query: String) =
        apiHelper.searchRecipesByIngredients(query)

    suspend fun getRandomRecipe(): RecipeResponseData {
        if (!NetworkUtil.isNetworkAvailable(mContext)) {
            val localRandomRecipe = mLocalRepo.getRandomRecipe()
            if (localRandomRecipe != null) return localRandomRecipe
        }
        return mRemoteRepo.getRandomRecipe()
    }

    suspend fun getAllLocalRecipes(): RecipeResponseData? {
        val recipesData = mLocalRepo.getRecipesByTag("")
        if (recipesData != null) return recipesData
        return null
    }

}