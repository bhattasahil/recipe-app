package com.sahil.recipeapp.di

import com.sahil.recipeapp.data.api.ApiHelper
import com.sahil.recipeapp.data.api.ApiHelperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface ApiHelperModule {
    @Binds
    fun bindApiHelper(impl: ApiHelperImpl): ApiHelper
}