package com.sahil.recipeapp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.sahil.recipeapp.data.local.WebsiteDao
import com.sahil.recipeapp.data.local.AppDatabase
import com.sahil.recipeapp.data.local.ingredientDb.IngredientDao
import com.sahil.recipeapp.data.local.randomRecipeDb.RandomRecipeDao
import com.sahil.recipeapp.data.local.recipesDb.RecipeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "recipe-palette"
        )
            .build()
    }

    @Provides
    fun provideWebsiteDao(database: AppDatabase): WebsiteDao {
        return database.websiteDao()
    }

    @Provides
    fun provideRandomRecipeDao(database: AppDatabase): RandomRecipeDao {
        return database.randomRecipeDao()
    }

    @Provides
    fun provideRecipeDao(database: AppDatabase): RecipeDao {
        return database.recipeDao()
    }

    @Provides
    fun provideIngredientDao(database: AppDatabase): IngredientDao {
        return database.ingredientDao()
    }

    @Provides
    fun provideSharedPreference(@ApplicationContext appContext: Context): SharedPreferences {
        return appContext.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
    }
}