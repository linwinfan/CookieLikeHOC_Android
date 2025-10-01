package com.cooklikehoc.recipes.di

import android.content.Context
import androidx.room.Room
import com.cooklikehoc.recipes.data.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideRecipeDatabase(
        @ApplicationContext context: Context
    ): RecipeDatabase {
        return Room.databaseBuilder(
            context,
            RecipeDatabase::class.java,
            "recipe_database_v2"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideRecipeDao(database: RecipeDatabase): RecipeDao {
        return database.recipeDao()
    }
    
    @Provides
    fun provideCategoryDao(database: RecipeDatabase): CategoryDao {
        return database.categoryDao()
    }
    
    @Provides
    fun provideFavoriteDao(database: RecipeDatabase): FavoriteDao {
        return database.favoriteDao()
    }
    
    @Provides
    fun provideRatingDao(database: RecipeDatabase): RatingDao {
        return database.ratingDao()
    }
}