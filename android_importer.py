#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Android 应用数据导入器
为 Android 应用生成优化的菜谱数据格式
"""

import json
import os
from pathlib import Path
from typing import Dict, List
from CookLikeHOCImporter import DataImporter, Recipe

class AndroidDataGenerator:
    """Android 数据生成器"""
    
    def __init__(self, importer: DataImporter):
        self.importer = importer
        self.recipes = importer.recipes
    
    def generate_room_database_schema(self) -> str:
        """生成 Room 数据库 Schema"""
        schema = '''
-- CookLikeHOC 菜谱数据库 Schema
-- 适用于 Android Room 数据库

CREATE TABLE recipes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    category TEXT NOT NULL,
    description TEXT,
    difficulty TEXT DEFAULT '未知',
    cooking_time INTEGER DEFAULT 0,
    servings INTEGER DEFAULT 1,
    ingredients TEXT, -- JSON 数组
    instructions TEXT, -- JSON 数组
    tips TEXT,
    nutrition TEXT,
    image_path TEXT,
    source_file TEXT,
    created_at INTEGER DEFAULT (strftime('%s', 'now')),
    updated_at INTEGER DEFAULT (strftime('%s', 'now'))
);

CREATE INDEX idx_recipes_category ON recipes(category);
CREATE INDEX idx_recipes_difficulty ON recipes(difficulty);
CREATE INDEX idx_recipes_cooking_time ON recipes(cooking_time);

-- 分类表
CREATE TABLE categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    display_name TEXT NOT NULL,
    description TEXT,
    icon TEXT,
    sort_order INTEGER DEFAULT 0
);

-- 插入分类数据
INSERT INTO categories (id, name, display_name, description, sort_order) VALUES
('staple', 'staple', '主食', '米饭、面条、馄饨等主食类', 1),
('stir_fry', 'stir_fry', '炒菜', '各种炒制菜品', 2),
('stew', 'stew', '炖菜', '炖煮类菜品', 3),
('steam', 'steam', '蒸菜', '蒸制类菜品', 4),
('fried', 'fried', '炸品', '油炸类食品', 5),
('grill', 'grill', '烤类', '烧烤类食品', 6),
('cold_dish', 'cold_dish', '凉拌', '凉拌菜品', 7),
('braised', 'braised', '卤菜', '卤制菜品', 8),
('breakfast', 'breakfast', '早餐', '早餐类食品', 9),
('soup', 'soup', '汤', '汤类菜品', 10),
('blanched', 'blanched', '烫菜', '烫制菜品', 11),
('casserole', 'casserole', '砂锅菜', '砂锅类菜品', 12),
('hot_pot', 'hot_pot', '煮锅', '火锅类菜品', 13),
('beverage', 'beverage', '饮品', '各种饮品', 14),
('seasoning', 'seasoning', '配料', '调料和配菜', 15);
'''
        return schema
    
    def generate_kotlin_data_classes(self) -> str:
        """生成 Kotlin 数据类"""
        kotlin_code = '''
// CookLikeHOC 菜谱数据模型
// 适用于 Android Kotlin 项目

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "category")
    val category: String,
    
    @ColumnInfo(name = "description")
    val description: String = "",
    
    @ColumnInfo(name = "difficulty")
    val difficulty: String = "未知",
    
    @ColumnInfo(name = "cooking_time")
    val cookingTime: Int = 0, // 分钟
    
    @ColumnInfo(name = "servings")
    val servings: Int = 1,
    
    @ColumnInfo(name = "ingredients")
    val ingredients: List<String> = emptyList(),
    
    @ColumnInfo(name = "instructions")
    val instructions: List<String> = emptyList(),
    
    @ColumnInfo(name = "tips")
    val tips: String = "",
    
    @ColumnInfo(name = "nutrition")
    val nutrition: String = "",
    
    @ColumnInfo(name = "image_path")
    val imagePath: String = "",
    
    @ColumnInfo(name = "source_file")
    val sourceFile: String = "",
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)

@Entity(tableName = "categories")
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
    val sortOrder: Int = 0
)

// DAO 接口
@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY title")
    suspend fun getAllRecipes(): List<Recipe>
    
    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY title")
    suspend fun getRecipesByCategory(category: String): List<Recipe>
    
    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' OR ingredients LIKE '%' || :query || '%'")
    suspend fun searchRecipes(query: String): List<Recipe>
    
    @Query("SELECT * FROM recipes WHERE difficulty = :difficulty ORDER BY title")
    suspend fun getRecipesByDifficulty(difficulty: String): List<Recipe>
    
    @Query("SELECT * FROM recipes WHERE cooking_time <= :maxTime ORDER BY cooking_time")
    suspend fun getQuickRecipes(maxTime: Int): List<Recipe>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<Recipe>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long
    
    @Update
    suspend fun updateRecipe(recipe: Recipe)
    
    @Delete
    suspend fun deleteRecipe(recipe: Recipe)
    
    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sort_order")
    suspend fun getAllCategories(): List<Category>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): Category?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)
}

// 数据库类
@Database(
    entities = [Recipe::class, Category::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CookLikeHOCDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: CookLikeHOCDatabase? = null
        
        fun getDatabase(context: Context): CookLikeHOCDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CookLikeHOCDatabase::class.java,
                    "cooklikehoc_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 类型转换器
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }
    
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}
'''
        return kotlin_code
    
    def generate_import_activity(self) -> str:
        """生成导入 Activity 代码"""
        activity_code = '''
// CookLikeHOC 数据导入 Activity
// 用于从项目目录或文件导入菜谱数据

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class ImporterActivity : AppCompatActivity() {
    
    private lateinit var database: CookLikeHOCDatabase
    private lateinit var recipeDao: RecipeDao
    private lateinit var categoryDao: CategoryDao
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                importFromFile(uri)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_importer)
        
        database = CookLikeHOCDatabase.getDatabase(this)
        recipeDao = database.recipeDao()
        categoryDao = database.categoryDao()
        
        setupUI()
        initializeCategories()
    }
    
    private fun setupUI() {
        findViewById<Button>(R.id.btnImportFromProject).setOnClickListener {
            importFromCookLikeHOCProject()
        }
        
        findViewById<Button>(R.id.btnImportFromFile).setOnClickListener {
            selectFile()
        }
        
        findViewById<Button>(R.id.btnImportFromAssets).setOnClickListener {
            importFromAssets()
        }
        
        findViewById<Button>(R.id.btnClearData).setOnClickListener {
            clearAllData()
        }
    }
    
    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun importFromCookLikeHOCProject() {
        showProgress("正在从 CookLikeHOC 项目导入...")
        
        lifecycleScope.launch {
            try {
                val projectPath = "e:\\UGit\\CookLikeHOC"
                val recipes = withContext(Dispatchers.IO) {
                    parseCookLikeHOCProject(projectPath)
                }
                
                withContext(Dispatchers.IO) {
                    recipeDao.insertRecipes(recipes)
                }
                
                hideProgress()
                showSuccess("成功导入 ${recipes.size} 个菜谱")
                
            } catch (e: Exception) {
                hideProgress()
                showError("导入失败: ${e.message}")
            }
        }
    }
    
    private fun importFromFile(uri: Uri) {
        showProgress("正在从文件导入...")
        
        lifecycleScope.launch {
            try {
                val recipes = withContext(Dispatchers.IO) {
                    parseRecipeFile(uri)
                }
                
                withContext(Dispatchers.IO) {
                    recipeDao.insertRecipes(recipes)
                }
                
                hideProgress()
                showSuccess("成功导入 ${recipes.size} 个菜谱")
                
            } catch (e: Exception) {
                hideProgress()
                showError("导入失败: ${e.message}")
            }
        }
    }
    
    private fun importFromAssets() {
        showProgress("正在从 Assets 导入...")
        
        lifecycleScope.launch {
            try {
                val recipes = withContext(Dispatchers.IO) {
                    parseAssetsRecipes()
                }
                
                withContext(Dispatchers.IO) {
                    recipeDao.insertRecipes(recipes)
                }
                
                hideProgress()
                showSuccess("成功导入 ${recipes.size} 个菜谱")
                
            } catch (e: Exception) {
                hideProgress()
                showError("导入失败: ${e.message}")
            }
        }
    }
    
    private suspend fun parseCookLikeHOCProject(projectPath: String): List<Recipe> {
        // 这里应该实现实际的项目解析逻辑
        // 可以调用 Python 脚本或实现 Kotlin 版本的解析器
        return emptyList()
    }
    
    private suspend fun parseRecipeFile(uri: Uri): List<Recipe> {
        val inputStream: InputStream = contentResolver.openInputStream(uri)
            ?: throw Exception("无法打开文件")
        
        val content = inputStream.bufferedReader().use { it.readText() }
        
        return when {
            content.trim().startsWith("{") -> {
                // JSON 格式
                val gson = Gson()
                val type = object : TypeToken<Map<String, Any>>() {}.type
                val data: Map<String, Any> = gson.fromJson(content, type)
                parseJsonRecipes(data)
            }
            content.contains("# ") -> {
                // Markdown 格式
                parseMarkdownRecipe(content)
            }
            else -> {
                throw Exception("不支持的文件格式")
            }
        }
    }
    
    private suspend fun parseAssetsRecipes(): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        
        try {
            val indexContent = assets.open("recipes_index.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val indexData: Map<String, Any> = gson.fromJson(indexContent, object : TypeToken<Map<String, Any>>() {}.type)
            
            val files = indexData["files"] as? List<String> ?: emptyList()
            
            for (fileName in files) {
                try {
                    val fileContent = assets.open(fileName).bufferedReader().use { it.readText() }
                    val categoryData: Map<String, Any> = gson.fromJson(fileContent, object : TypeToken<Map<String, Any>>() {}.type)
                    val categoryRecipes = parseJsonRecipes(categoryData)
                    recipes.addAll(categoryRecipes)
                } catch (e: Exception) {
                    // 记录错误但继续处理其他文件
                }
            }
        } catch (e: Exception) {
            throw Exception("Assets 导入失败: ${e.message}")
        }
        
        return recipes
    }
    
    private fun parseJsonRecipes(data: Map<String, Any>): List<Recipe> {
        // 实现 JSON 数据解析逻辑
        return emptyList()
    }
    
    private fun parseMarkdownRecipe(content: String): List<Recipe> {
        // 实现 Markdown 解析逻辑
        return emptyList()
    }
    
    private fun initializeCategories() {
        lifecycleScope.launch {
            val categories = listOf(
                Category("staple", "staple", "主食", "米饭、面条、馄饨等主食类", "", 1),
                Category("stir_fry", "stir_fry", "炒菜", "各种炒制菜品", "", 2),
                Category("stew", "stew", "炖菜", "炖煮类菜品", "", 3),
                Category("steam", "steam", "蒸菜", "蒸制类菜品", "", 4),
                Category("fried", "fried", "炸品", "油炸类食品", "", 5),
                Category("grill", "grill", "烤类", "烧烤类食品", "", 6),
                Category("cold_dish", "cold_dish", "凉拌", "凉拌菜品", "", 7),
                Category("braised", "braised", "卤菜", "卤制菜品", "", 8),
                Category("breakfast", "breakfast", "早餐", "早餐类食品", "", 9),
                Category("soup", "soup", "汤", "汤类菜品", "", 10),
                Category("blanched", "blanched", "烫菜", "烫制菜品", "", 11),
                Category("casserole", "casserole", "砂锅菜", "砂锅类菜品", "", 12),
                Category("hot_pot", "hot_pot", "煮锅", "火锅类菜品", "", 13),
                Category("beverage", "beverage", "饮品", "各种饮品", "", 14),
                Category("seasoning", "seasoning", "配料", "调料和配菜", "", 15)
            )
            
            withContext(Dispatchers.IO) {
                categoryDao.insertCategories(categories)
            }
        }
    }
    
    private fun clearAllData() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                recipeDao.deleteAllRecipes()
            }
            showSuccess("数据已清空")
        }
    }
    
    private fun showProgress(message: String) {
        // 显示进度对话框
    }
    
    private fun hideProgress() {
        // 隐藏进度对话框
    }
    
    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, "错误: $message", Toast.LENGTH_LONG).show()
    }
}
'''
        return activity_code
    
    def generate_all_android_files(self, output_dir: str = "android_generated"):
        """生成所有 Android 相关文件"""
        os.makedirs(output_dir, exist_ok=True)
        
        # 生成数据库 Schema
        with open(os.path.join(output_dir, "database_schema.sql"), 'w', encoding='utf-8') as f:
            f.write(self.generate_room_database_schema())
        
        # 生成 Kotlin 数据类
        with open(os.path.join(output_dir, "DataModels.kt"), 'w', encoding='utf-8') as f:
            f.write(self.generate_kotlin_data_classes())
        
        # 生成导入 Activity
        with open(os.path.join(output_dir, "ImporterActivity.kt"), 'w', encoding='utf-8') as f:
            f.write(self.generate_import_activity())
        
        print(f"Android 代码文件已生成到: {output_dir}")
        return output_dir

def main():
    """主函数 - 生成 Android 优化的数据和代码"""
    try:
        # 首先导入数据
        importer = DataImporter()
        importer.import_all_recipes()
        
        # 生成 Android 相关文件
        android_generator = AndroidDataGenerator(importer)
        
        # 导出 Android Assets
        assets_dir = importer.export_to_android_assets("android_assets")
        
        # 生成 Android 代码文件
        code_dir = android_generator.generate_all_android_files("android_generated")
        
        print("\n🎉 Android 数据和代码生成完成!")
        print(f"📱 Assets 目录: {assets_dir}")
        print(f"💻 代码文件目录: {code_dir}")
        
        return True
        
    except Exception as e:
        print(f"❌ 生成失败: {e}")
        return False

if __name__ == "__main__":
    main()