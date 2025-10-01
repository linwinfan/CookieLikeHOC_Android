package com.cooklikehoc.recipes.data.database

import androidx.room.*
import com.cooklikehoc.recipes.data.model.Rating
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {
    
    @Query("SELECT * FROM ratings WHERE recipe_id = :recipeId ORDER BY created_at DESC")
    fun getRatingsByRecipeId(recipeId: Long): Flow<List<Rating>>
    
    @Query("SELECT AVG(rating) FROM ratings WHERE recipe_id = :recipeId")
    suspend fun getAverageRatingByRecipeId(recipeId: Long): Float?
    
    @Query("SELECT COUNT(*) FROM ratings WHERE recipe_id = :recipeId")
    suspend fun getRatingCountByRecipeId(recipeId: Long): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: Rating)
    
    @Update
    suspend fun updateRating(rating: Rating)
    
    @Delete
    suspend fun deleteRating(rating: Rating)
    
    @Query("DELETE FROM ratings WHERE recipe_id = :recipeId")
    suspend fun deleteRatingsByRecipeId(recipeId: Long)
    
    @Query("DELETE FROM ratings")
    suspend fun deleteAllRatings()
}