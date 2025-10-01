package com.cooklikehoc.recipes.utils

import android.content.Context
import com.cooklikehoc.recipes.data.repository.RecipeRepository
import com.cooklikehoc.recipes.data.repository.ImportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataImporter @Inject constructor(
    private val repository: RecipeRepository
) {
    
    suspend fun importAllData(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            when (val result = repository.importRecipesFromAssets()) {
                is ImportResult.Success -> {
                    Result.success("成功导入 ${result.recipeCount} 个菜谱，${result.categoryCount} 个分类")
                }
                is ImportResult.AlreadyImported -> {
                    Result.success("数据已存在，跳过导入")
                }
                is ImportResult.Error -> {
                    Result.failure(Exception(result.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkDataExists(): Boolean = withContext(Dispatchers.IO) {
        repository.getTotalRecipeCount() > 0
    }
    
    suspend fun clearAllData(): Result<String> = withContext(Dispatchers.IO) {
        try {
            repository.clearAllData()
            Result.success("数据清理完成")
        } catch (e: Exception) {
            Result.failure(Exception("清理数据失败: ${e.message}"))
        }
    }
}