package com.cooklikehoc.recipes.di

import com.cooklikehoc.recipes.data.repository.RecipeRepository
import com.cooklikehoc.recipes.data.repository.RecipeRepositoryImpl
import com.cooklikehoc.recipes.utils.DataImporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        recipeRepositoryImpl: RecipeRepositoryImpl
    ): RecipeRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Singleton
    @dagger.Provides
    fun provideDataImporter(
        repository: RecipeRepository
    ): DataImporter {
        return DataImporter(repository)
    }
}