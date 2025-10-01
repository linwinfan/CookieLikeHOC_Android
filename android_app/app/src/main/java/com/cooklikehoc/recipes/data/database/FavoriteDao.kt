package com.cooklikehoc.recipes.data.database

import androidx.room.*
import com.cooklikehoc.recipes.data.model.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    
    @Query("SELECT * FROM favorites ORDER BY created_at DESC")
    fun getAllFavorites(): Flow<List<Favorite>>
    
    @Query("SELECT * FROM favorites WHERE recipe_id = :recipeId")
    suspend fun getFavoriteByRecipeId(recipeId: Long): Favorite?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)
    
    @Delete
    suspend fun deleteFavorite(favorite: Favorite)
    
    @Query("DELETE FROM favorites WHERE recipe_id = :recipeId")
    suspend fun deleteFavoriteByRecipeId(recipeId: Long)
    
    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()
    
    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int
}