package com.sahil.recipeapp.uis.viewstate

import com.sahil.recipeapp.data.model.ExtendedIngredients
import com.sahil.recipeapp.data.model.RecipeData
import com.sahil.recipeapp.data.model.RecipeResponseData
import com.sahil.recipeapp.data.model.SearchedRecipe
import com.sahil.recipeapp.data.model.SearchedRecipeData
import com.sahil.recipeapp.data.model.WebsiteData

sealed class DataState {
    object Inactive : DataState()
    object Loading : DataState()
    data class ResponseData(val recipeResponseData: RecipeResponseData) : DataState()

    data class RecipeDetail(val recipeData: RecipeData) : DataState()

    data class Error(val error: String?) : DataState()

    data class AddToFavoriteResponse(val recipe: RecipeData, val isFromHome: Boolean) : DataState()

    data class FavoriteResponse(val recipes: ArrayList<RecipeData>?) : DataState()

    data class SearchRecipes(val searchRecipeData: SearchedRecipeData) : DataState()

    data class SearchRecipesByNutrients(val searchedRecipes: ArrayList<SearchedRecipe>) :
        DataState()

    data class IngredientResponse(val ingredients: ArrayList<ExtendedIngredients>) : DataState()

    data class AddToShoppingList(val ingredients: List<ExtendedIngredients>) : DataState()

    data class FetchWebsiteList(val websites: List<WebsiteData>) : DataState()

    data class RandomRecipe(val recipeResponseData: RecipeResponseData) : DataState()
}
