package com.sahil.recipeapp.di

import android.content.Context
import com.sahil.recipeapp.data.api.ApiHelper
import com.sahil.recipeapp.data.local.randomRecipeDb.RandomRecipeDao
import com.sahil.recipeapp.data.local.recipesDb.RecipeDao
import com.sahil.recipeapp.data.repository.MainRepository
import com.sahil.recipeapp.data.repository.local.LocalRepo
import com.sahil.recipeapp.data.repository.remote.RemoteRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
internal object ViewModelMainModule {

    @Provides
    @ViewModelScoped
    fun provideLocalRepo(
        randomRecipeDao: RandomRecipeDao, recipeDao: RecipeDao
    ) = LocalRepo(randomRecipeDao, recipeDao)

    @Provides
    @ViewModelScoped
    fun provideRemoteRepo(
        apiHelper: ApiHelper,
        randomRecipeDao: RandomRecipeDao,
        recipeDao: RecipeDao
    ) = RemoteRepo(apiHelper, randomRecipeDao, recipeDao)

    @Provides
    @ViewModelScoped
    fun provideMainRepo(
        @ApplicationContext context: Context,
        apiHelper: ApiHelper, localRepo: LocalRepo, remoteRepo: RemoteRepo
    ) = MainRepository(context, apiHelper, localRepo, remoteRepo)

}