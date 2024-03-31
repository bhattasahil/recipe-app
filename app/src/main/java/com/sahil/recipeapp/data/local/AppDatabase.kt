package com.sahil.recipeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sahil.recipeapp.data.local.ingredientDb.IngredientDao
import com.sahil.recipeapp.data.local.randomRecipeDb.RandomRecipeDao
import com.sahil.recipeapp.data.local.recipesDb.RecipeDao
import com.sahil.recipeapp.data.model.ExtendedIngredients
import com.sahil.recipeapp.data.model.RecipeData
import com.sahil.recipeapp.data.model.WebsiteData

@Database(
    entities = [RecipeData::class, ExtendedIngredients::class, WebsiteData::class],
    version = 1
)
@TypeConverters(RecipeTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun websiteDao(): WebsiteDao
    abstract fun randomRecipeDao(): RandomRecipeDao
    abstract fun recipeDao(): RecipeDao
}
