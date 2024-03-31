package com.sahil.recipeapp.data.api


import com.sahil.recipeapp.data.model.RecipeData
import com.sahil.recipeapp.data.model.RecipeResponseData
import com.sahil.recipeapp.data.model.SearchedRecipe
import com.sahil.recipeapp.data.model.SearchedRecipeData

interface ApiHelper {
    suspend fun getData(tag: String): RecipeResponseData

    suspend fun searchRecipes(query: String): SearchedRecipeData

    suspend fun getRecipeDetail(id: Int): RecipeData

    suspend fun searchRecipesByIngredients(query: String): ArrayList<SearchedRecipe>

    suspend fun getRandomRecipe(): RecipeResponseData
}