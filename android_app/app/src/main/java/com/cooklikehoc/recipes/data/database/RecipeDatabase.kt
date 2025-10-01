package com.cooklikehoc.recipes.data.database

import android.content.Context
import androidx.room.*
import com.cooklikehoc.recipes.data.model.*

@Database(
    entities = [Recipe::class, Category::class, Favorite::class, Rating::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RecipeDatabase : RoomDatabase() {
    
    abstract fun recipeDao(): RecipeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun ratingDao(): RatingDao
    
    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null
        
        fun getDatabase(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "cooklikehoc_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}