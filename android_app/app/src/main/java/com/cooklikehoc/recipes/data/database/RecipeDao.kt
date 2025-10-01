package com.cooklikehoc.recipes.data.database

import androidx.room.*
import com.cooklikehoc.recipes.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    
    // 基本查询
    @Query("SELECT * FROM recipes ORDER BY title")
    fun getAllRecipes(): Flow<List<Recipe>>
    
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): Recipe?
    
    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY title")
    fun getRecipesByCategory(category: String): Flow<List<Recipe>>
    
    // 搜索功能
    @Query("""
        SELECT * FROM recipes 
        WHERE title LIKE '%' || :query || '%' 
        OR ingredients LIKE '%' || :query || '%'
        OR instructions LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN title LIKE '%' || :query || '%' THEN 1
                WHEN ingredients LIKE '%' || :query || '%' THEN 2
                ELSE 3
            END, title
    """)
    fun searchRecipes(query: String): Flow<List<Recipe>>
    
    // 筛选功能已移除 - 原始数据中没有difficulty, cooking_time, servings字段
    
    // 收藏功能
    @Query("""
        SELECT r.* FROM recipes r 
        INNER JOIN favorites f ON r.id = f.recipe_id 
        ORDER BY f.created_at DESC
    """)
    fun getFavoriteRecipes(): Flow<List<Recipe>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE recipe_id = :recipeId)")
    suspend fun isRecipeFavorite(recipeId: Long): Boolean
    
    // 评分功能
    @Query("SELECT * FROM recipes WHERE rating >= :minRating ORDER BY rating DESC, title")
    fun getHighRatedRecipes(minRating: Float): Flow<List<Recipe>>
    
    @Query("SELECT * FROM recipes ORDER BY rating DESC, title LIMIT :limit")
    fun getTopRatedRecipes(limit: Int): Flow<List<Recipe>>
    
    // 随机推荐
    @Query("SELECT * FROM recipes ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomRecipes(limit: Int): List<Recipe>
    
    // 最近添加
    @Query("SELECT * FROM recipes ORDER BY created_at DESC LIMIT :limit")
    fun getRecentRecipes(limit: Int): Flow<List<Recipe>>
    
    // 统计信息
    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getTotalRecipeCount(): Int
    
    @Query("SELECT COUNT(*) FROM recipes WHERE category = :category")
    suspend fun getRecipeCountByCategory(category: String): Int
    
    @Query("SELECT category as categoryId, category as categoryName, COUNT(*) as recipeCount, AVG(rating) as averageRating FROM recipes GROUP BY category")
    suspend fun getCategoryStats(): List<CategoryStat>
    
    // 插入和更新
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<Recipe>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long
    
    @Update
    suspend fun updateRecipe(recipe: Recipe)
    
    @Query("UPDATE recipes SET is_favorite = :isFavorite WHERE id = :recipeId")
    suspend fun updateRecipeFavoriteStatus(recipeId: Long, isFavorite: Boolean)
    
    @Query("UPDATE recipes SET rating = :rating WHERE id = :recipeId")
    suspend fun updateRecipeRating(recipeId: Long, rating: Float)
    
    // 删除
    @Delete
    suspend fun deleteRecipe(recipe: Recipe)
    
    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: Long)
    
    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()
}

