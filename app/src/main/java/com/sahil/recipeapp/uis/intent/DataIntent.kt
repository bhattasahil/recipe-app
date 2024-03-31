package com.sahil.recipeapp.uis.intent

import com.sahil.recipeapp.data.model.ExtendedIngredients
import com.sahil.recipeapp.data.model.RecipeData
import com.sahil.recipeapp.data.model.SearchedRecipe

sealed class DataIntent {
    data class FetchRecipeData(
        val tag: String
    ) : DataIntent()

    data class FetchRecipeDetail(
        val recipeId: Int
    ) : DataIntent()

    data class ChangeFavoriteStatus(
        val recipe: RecipeData,
        val isToFavorite: Boolean,
        val isFromHome : Boolean
    ) : DataIntent()

    object FetchFavoriteRecipe : DataIntent()

    data class SearchRecipe(val query: String) : DataIntent()

    data class ChangeFavoriteStatusFromSearch(
        val recipe: SearchedRecipe,
        val isToFavorite: Boolean
    ) : DataIntent()

    data class SearchRecipesByNutrients(val query: String) : DataIntent()

    object FetchShoppingListData : DataIntent()

    data class AddToShoppingList(val ingredients: List<ExtendedIngredients>): DataIntent()

    data class RemoveFromShoppingList(val ingredients: ExtendedIngredients): DataIntent()

    object FetchWebsiteList: DataIntent()

    object FetchRandomRecipe : DataIntent()

}
