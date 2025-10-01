package com.cooklikehoc.recipes.data.database

import androidx.room.*
import com.cooklikehoc.recipes.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories ORDER BY sort_order ASC")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): Category?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)
    
    @Update
    suspend fun updateCategory(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
    
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
    
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}