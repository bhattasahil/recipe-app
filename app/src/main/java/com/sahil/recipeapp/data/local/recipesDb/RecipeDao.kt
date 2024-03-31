package com.sahil.recipeapp.data.local.recipesDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sahil.recipeapp.data.model.RecipeData

/**
 * Used to store recipes and handle favorite status
 */
@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAllRecipes(recipes: List<RecipeData>)

    @Query("Select * From recipes WHERE tagToBeSearchedBy = :tag")
    suspend fun getRecipes(tag: String): List<RecipeData>?

    @Query("Delete FROM recipes WHERE id in (:recipeIds)")
    suspend fun removePreviousRecipes(recipeIds: List<Int>)

    @Query("Select * From recipes WHERE id = :recipeId AND isFavorite = 1")
    suspend fun getFavoriteRecipe(recipeId: Int): RecipeData?

    @Query("Select * From recipes WHERE isFavorite = 1")
    suspend fun getAllFavoriteRecipes(): List<RecipeData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteRecipe(recipe: RecipeData)

    /**
     * Updates isFavorite flag of recipe
     * @param recipe
     */
    @Update
    suspend fun changeRecipeFavoriteStatus(recipe: RecipeData)
}