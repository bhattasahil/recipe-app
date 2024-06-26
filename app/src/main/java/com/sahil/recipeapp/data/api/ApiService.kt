package com.sahil.recipeapp.data.api

import com.sahil.recipeapp.data.model.RecipeData
import retrofit2.http.GET
import com.sahil.recipeapp.data.model.RecipeResponseData
import com.sahil.recipeapp.data.model.SearchedRecipe
import com.sahil.recipeapp.data.model.SearchedRecipeData
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("recipes/random")
    suspend fun getData(
        @Query("tags") tags: String,
        @Query("number") number: Int = 30
    ): RecipeResponseData

    @GET("recipes/{id}/information")
    suspend fun getRecipeDetail(
        @Path("id") id: Int,
        @Query("includeNutrition") number: Boolean = true
    ): RecipeData

    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("query") query: String,
        @Query("number") number: Int = 30
    ): SearchedRecipeData

    @GET("recipes/findByIngredients")
    suspend fun searchRecipesByIngredients(
        @Query("ingredients") query: String,
        @Query("number") number: Int = 30
    ): ArrayList<SearchedRecipe>

    @GET("recipes/random")
    suspend fun getRandomRecipe(
        @Query("number") number: Int = 1
    ): RecipeResponseData
}