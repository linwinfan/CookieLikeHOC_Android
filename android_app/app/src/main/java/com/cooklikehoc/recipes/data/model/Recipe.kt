package com.cooklikehoc.recipes.data.model

import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(tableName = "recipes")
@Parcelize
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "category")
    val category: String,
    
    @ColumnInfo(name = "description")
    val description: String = "",
    

    
    @ColumnInfo(name = "ingredients")
    val ingredients: List<String> = emptyList(),
    
    @ColumnInfo(name = "instructions")
    val instructions: List<String> = emptyList(),
    

    
    @ColumnInfo(name = "image_path")
    @SerializedName("image_path")
    val imagePath: String = "",
    
    @ColumnInfo(name = "source_file")
    @SerializedName("source_file")
    val sourceFile: String = "",
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "rating")
    val rating: Float = 0f,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
) : Parcelable {
    

    
    // 获取分类显示名称
    fun getCategoryDisplayName(): String {
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

            else -> "其他"
        }
    }
    

}

@Entity(tableName = "categories")
@Parcelize
data class Category(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "display_name")
    val displayName: String,
    
    @ColumnInfo(name = "description")
    val description: String = "",
    
    @ColumnInfo(name = "icon")
    val icon: String = "",
    
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
    
    @ColumnInfo(name = "recipe_count")
    val recipeCount: Int = 0
) : Parcelable

// 导入数据模型
data class RecipeImportData(
    val metadata: ImportMetadata,
    val recipes: List<Recipe>
)

data class ImportMetadata(
    val source: String,
    @SerializedName("import_time")
    val importTime: String,
    @SerializedName("total_recipes")
    val totalRecipes: Int,
    val categories: List<String>
)

data class CategoryRecipeData(
    val category: String,
    val count: Int,
    val recipes: List<Recipe>
)

// 用于统计的数据类
data class CategoryStat(
    val categoryId: String,
    val categoryName: String,
    val recipeCount: Int,
    val averageRating: Float
)

// 搜索结果
data class SearchResult(
    val recipe: Recipe,
    val matchType: SearchMatchType,
    val matchText: String
)

enum class SearchMatchType {
    TITLE,
    INGREDIENT,
    INSTRUCTION,
    CATEGORY
}

// 收藏夹
@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "recipe_id")
    val recipeId: Long,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)

// 评分
@Entity(tableName = "ratings")
data class Rating(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "recipe_id")
    val recipeId: Long,
    
    @ColumnInfo(name = "rating")
    val rating: Float,
    
    @ColumnInfo(name = "comment")
    val comment: String = "",
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)