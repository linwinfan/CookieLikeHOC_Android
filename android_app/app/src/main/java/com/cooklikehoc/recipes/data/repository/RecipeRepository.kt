package com.cooklikehoc.recipes.data.repository

import android.content.Context
import com.cooklikehoc.recipes.data.database.*
import com.cooklikehoc.recipes.data.model.*
import com.cooklikehoc.recipes.data.model.CategoryStat
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

interface RecipeRepository {
    // 菜谱相关
    fun getAllRecipes(): Flow<List<Recipe>>
    suspend fun getRecipeById(id: Long): Recipe?
    fun getRecipesByCategory(category: String): Flow<List<Recipe>>
    fun searchRecipes(query: String): Flow<List<Recipe>>

    fun getFavoriteRecipes(): Flow<List<Recipe>>
    fun getHighRatedRecipes(minRating: Float): Flow<List<Recipe>>
    fun getTopRatedRecipes(limit: Int): Flow<List<Recipe>>
    suspend fun getRandomRecipes(limit: Int): List<Recipe>
    fun getRecentRecipes(limit: Int): Flow<List<Recipe>>
    
    // 分类相关
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: String): Category?
    
    // 收藏相关
    suspend fun toggleFavorite(recipeId: Long)
    suspend fun isRecipeFavorite(recipeId: Long): Boolean
    
    // 评分相关
    suspend fun rateRecipe(recipeId: Long, rating: Float, comment: String)
    suspend fun getAverageRating(recipeId: Long): Float
    fun getRatingsByRecipeId(recipeId: Long): Flow<List<Rating>>
    
    // 统计信息
    suspend fun getTotalRecipeCount(): Int
    suspend fun getCategoryStats(): List<CategoryStat>
    
    // 数据导入
    suspend fun importRecipesFromAssets(): ImportResult
    
    // 获取配料数据
    suspend fun getSeasoningData(): List<Recipe>
    
    // 清除所有数据
    suspend fun clearAllData()
}

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: RecipeDatabase
) : RecipeRepository {
    
    private val recipeDao = database.recipeDao()
    private val categoryDao = database.categoryDao()
    private val favoriteDao = database.favoriteDao()
    private val ratingDao = database.ratingDao()
    private val gson = Gson()
    
    // 菜谱相关
    override fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()
    
    override suspend fun getRecipeById(id: Long): Recipe? = recipeDao.getRecipeById(id)
    
    override fun getRecipesByCategory(category: String): Flow<List<Recipe>> = 
        recipeDao.getRecipesByCategory(category)
    
    override fun searchRecipes(query: String): Flow<List<Recipe>> = recipeDao.searchRecipes(query)
    

    
    override fun getFavoriteRecipes(): Flow<List<Recipe>> = recipeDao.getFavoriteRecipes()
    
    override fun getHighRatedRecipes(minRating: Float): Flow<List<Recipe>> = 
        recipeDao.getHighRatedRecipes(minRating)
    
    override fun getTopRatedRecipes(limit: Int): Flow<List<Recipe>> = 
        recipeDao.getTopRatedRecipes(limit)
    
    override suspend fun getRandomRecipes(limit: Int): List<Recipe> = recipeDao.getRandomRecipes(limit)
    
    override fun getRecentRecipes(limit: Int): Flow<List<Recipe>> = recipeDao.getRecentRecipes(limit)
    
    // 分类相关
    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    
    override suspend fun getCategoryById(id: String): Category? = categoryDao.getCategoryById(id)
    
    // 收藏相关
    override suspend fun toggleFavorite(recipeId: Long) {
        val isFavorite = recipeDao.isRecipeFavorite(recipeId)
        if (isFavorite) {
            favoriteDao.deleteFavoriteByRecipeId(recipeId)
        } else {
            favoriteDao.insertFavorite(Favorite(recipeId = recipeId))
        }
        recipeDao.updateRecipeFavoriteStatus(recipeId, !isFavorite)
    }
    
    override suspend fun isRecipeFavorite(recipeId: Long): Boolean = recipeDao.isRecipeFavorite(recipeId)
    
    // 评分相关
    override suspend fun rateRecipe(recipeId: Long, rating: Float, comment: String) {
        ratingDao.insertRating(Rating(recipeId = recipeId, rating = rating, comment = comment))
        val averageRating = ratingDao.getAverageRatingByRecipeId(recipeId) ?: 0f
        recipeDao.updateRecipeRating(recipeId, averageRating)
    }
    
    override suspend fun getAverageRating(recipeId: Long): Float = 
        ratingDao.getAverageRatingByRecipeId(recipeId) ?: 0f
    
    override fun getRatingsByRecipeId(recipeId: Long): Flow<List<Rating>> = 
        ratingDao.getRatingsByRecipeId(recipeId)
    
    // 统计信息
    override suspend fun getTotalRecipeCount(): Int = recipeDao.getTotalRecipeCount()
    
    override suspend fun getCategoryStats(): List<CategoryStat> = recipeDao.getCategoryStats()
    
    // 数据导入
    override suspend fun importRecipesFromAssets(): ImportResult = withContext(Dispatchers.IO) {
        try {
            // 检查是否已经导入过数据
            val existingCount = recipeDao.getTotalRecipeCount()
            if (existingCount > 0) {
                return@withContext ImportResult.AlreadyImported(existingCount)
            }
            
            // 读取元数据文件
            val metadataContent = context.assets.open("metadata.json")
                .bufferedReader().use { it.readText() }
            val metadata = gson.fromJson(metadataContent, Map::class.java) as Map<String, Any>
            val categoryList = metadata["categories"] as List<String>
            
            val allRecipes = mutableListOf<Recipe>()
            val categories = mutableListOf<Category>()
            
            // 导入每个分类文件，跳过配料分类
            categoryList.forEachIndexed { index, categoryId ->
                // 跳过配料分类
                if (categoryId == "seasoning") {
                    android.util.Log.d("RecipeRepository", "跳过配料分类: $categoryId")
                    return@forEachIndexed
                }
                
                try {
                    val fileName = "categories/${categoryId}_recipes.json"
                    val fileContent = context.assets.open(fileName)
                        .bufferedReader().use { it.readText() }
                    val categoryData = gson.fromJson(fileContent, CategoryRecipeData::class.java)
                    
                    // 为每个菜谱设置时间戳
                    val recipesWithTimestamp = categoryData.recipes.map { recipe ->
                        recipe.copy(
                            createdAt = Date(),
                            updatedAt = Date()
                        )
                    }
                    allRecipes.addAll(recipesWithTimestamp)
                    
                    // 创建分类信息
                    val category = Category(
                        id = categoryData.category,
                        name = categoryData.category,
                        displayName = getCategoryDisplayName(categoryData.category),
                        description = "${getCategoryDisplayName(categoryData.category)}类菜品",
                        sortOrder = index + 1,
                        recipeCount = categoryData.count
                    )
                    categories.add(category)
                    
                } catch (e: Exception) {
                    // 记录错误但继续处理其他文件
                    android.util.Log.e("RecipeRepository", "导入分类 $categoryId 失败: ${e.message}")
                }
            }
            
            // 如果分类文件导入失败，尝试导入主文件
            if (allRecipes.isEmpty()) {
                try {
                    val mainContent = context.assets.open("cooklikehoc_recipes.json")
                        .bufferedReader().use { it.readText() }
                    val recipeList = gson.fromJson(mainContent, Array<Recipe>::class.java).toList()
                    
                    // 过滤掉配料分类的菜谱
                    val filteredRecipeList = recipeList.filter { it.category != "seasoning" }
                    
                    // 为每个菜谱设置时间戳
                    val recipesWithTimestamp = filteredRecipeList.map { recipe ->
                        recipe.copy(
                            createdAt = Date(),
                            updatedAt = Date()
                        )
                    }
                    allRecipes.addAll(recipesWithTimestamp)
                    
                    // 从菜谱中提取分类信息，过滤掉配料分类
                    val categoryMap = filteredRecipeList.groupBy { it.category }
                    categoryMap.entries.forEachIndexed { index, (categoryId, recipes) ->
                        val category = Category(
                            id = categoryId,
                            name = categoryId,
                            displayName = getCategoryDisplayName(categoryId),
                            description = "${getCategoryDisplayName(categoryId)}类菜品",
                            sortOrder = index + 1,
                            recipeCount = recipes.size
                        )
                        categories.add(category)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RecipeRepository", "导入主文件失败: ${e.message}")
                    return@withContext ImportResult.Error("导入数据失败: ${e.message}")
                }
            }
            
            // 插入数据
            if (categories.isNotEmpty()) {
                categoryDao.insertCategories(categories)
            }
            if (allRecipes.isNotEmpty()) {
                recipeDao.insertRecipes(allRecipes)
            }
            
            ImportResult.Success(allRecipes.size, categories.size)
            
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "导入失败")
        }
    }
    
    private fun getCategoryDisplayName(category: String): String {
        return when (category) {
            "staple" -> "主食"
            "stir_fry" -> "炒菜"
            "stew" -> "炖菜"
            "steam" -> "蒸菜"
            "grill" -> "烤类"
            "fried" -> "炸品"
            "cold_dish" -> "凉拌"
            "braised" -> "卤菜"
            "breakfast" -> "早餐"
            "soup" -> "汤"
            "blanched" -> "烫菜"
            "casserole" -> "砂锅菜"
            "hot_pot" -> "煮锅"
            "beverage" -> "饮品"
            "seasoning" -> "菜谱配料"
            else -> "其他"
        }
    }
    
    // 获取配料数据
    override suspend fun getSeasoningData(): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            val fileName = "categories/seasoning_recipes.json"
            val fileContent = context.assets.open(fileName)
                .bufferedReader().use { it.readText() }
            val categoryData = gson.fromJson(fileContent, CategoryRecipeData::class.java)
            
            // 返回配料数据，不需要时间戳
            categoryData.recipes
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "读取配料数据失败: ${e.message}")
            emptyList()
        }
    }
    
    // 清除所有数据
    override suspend fun clearAllData() {
        ratingDao.deleteRatingsByRecipeId(0) // 删除所有评分
        favoriteDao.deleteAllFavorites()
        recipeDao.deleteAllRecipes()
        categoryDao.deleteAllCategories()
    }
}

sealed class ImportResult {
    data class Success(val recipeCount: Int, val categoryCount: Int) : ImportResult()
    data class AlreadyImported(val existingCount: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
}