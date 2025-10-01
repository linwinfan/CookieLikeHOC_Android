package com.cooklikehoc.recipes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooklikehoc.recipes.data.model.Recipe
import com.cooklikehoc.recipes.data.model.Category
import com.cooklikehoc.recipes.data.repository.RecipeRepository
import com.cooklikehoc.recipes.data.repository.ImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    // 所有菜谱
    val allRecipes: StateFlow<List<Recipe>> = repository.getAllRecipes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 分类列表
    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 搜索结果
    val searchResults: StateFlow<List<Recipe>> = searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchRecipes(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 缓存分类菜谱的StateFlow
    private val categoryRecipesCache = mutableMapOf<String, StateFlow<List<Recipe>>>()
    
    // 获取分类菜谱的方法
    fun getCategoryRecipes(categoryId: String): StateFlow<List<Recipe>> {
        return categoryRecipesCache.getOrPut(categoryId) {
            android.util.Log.d("RecipeViewModel", "创建分类菜谱StateFlow: $categoryId")
            repository.getRecipesByCategory(categoryId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        }
    }
    
    // 收藏菜谱
    val favoriteRecipes: StateFlow<List<Recipe>> = repository.getFavoriteRecipes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 推荐菜谱
    val recommendedRecipes: StateFlow<List<Recipe>> = repository.getTopRatedRecipes(10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 最近菜谱
    val recentRecipes: StateFlow<List<Recipe>> = repository.getRecentRecipes(10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 配料列表 - 从assets直接读取，不存储到数据库
    private val _seasoningRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val seasoningRecipes: StateFlow<List<Recipe>> = _seasoningRecipes.asStateFlow()
    
    init {
        // 应用启动时检查并导入数据
        viewModelScope.launch {
            android.util.Log.d("RecipeViewModel", "开始初始化数据...")
            importRecipesIfNeeded()
            
            // 加载配料数据
            loadSeasoningData()
            
            // 检查数据是否成功导入
            val recipeCount = repository.getTotalRecipeCount()
            val categoryCount = repository.getAllCategories().first().size
            android.util.Log.d("RecipeViewModel", "数据检查: $recipeCount 个菜谱, $categoryCount 个分类")
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }
    
    fun toggleFavorite(recipeId: Long) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(recipeId)
                _uiState.value = _uiState.value.copy(
                    message = "收藏状态已更新"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "更新收藏失败: ${e.message}"
                )
            }
        }
    }
    
    fun rateRecipe(recipeId: Long, rating: Float, comment: String = "") {
        viewModelScope.launch {
            try {
                repository.rateRecipe(recipeId, rating, comment)
                _uiState.value = _uiState.value.copy(
                    message = "评分已保存"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "保存评分失败: ${e.message}"
                )
            }
        }
    }
    
    suspend fun getRecipeById(id: Long): Recipe? {
        return repository.getRecipeById(id)
    }
    
    suspend fun isRecipeFavorite(recipeId: Long): Boolean {
        return repository.isRecipeFavorite(recipeId)
    }
    
    fun getRandomRecipes(limit: Int = 5) {
        viewModelScope.launch {
            try {
                val randomRecipes = repository.getRandomRecipes(limit)
                _uiState.value = _uiState.value.copy(
                    randomRecipes = randomRecipes
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "获取随机菜谱失败: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun importRecipesIfNeeded() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        try {
            android.util.Log.d("RecipeViewModel", "开始导入数据...")
            when (val result = repository.importRecipesFromAssets()) {
                is ImportResult.Success -> {
                    android.util.Log.d("RecipeViewModel", "数据导入成功: ${result.recipeCount} 个菜谱, ${result.categoryCount} 个分类")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "成功导入 ${result.recipeCount} 个菜谱"
                    )
                }
                is ImportResult.AlreadyImported -> {
                    android.util.Log.d("RecipeViewModel", "数据已存在: ${result.existingCount} 个菜谱")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                }
                is ImportResult.Error -> {
                    android.util.Log.e("RecipeViewModel", "数据导入失败: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("RecipeViewModel", "导入数据异常: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "导入数据失败: ${e.message}"
            )
        }
    }
    
    private suspend fun loadSeasoningData() {
        try {
            android.util.Log.d("RecipeViewModel", "开始加载配料数据...")
            val seasoningData = repository.getSeasoningData()
            _seasoningRecipes.value = seasoningData
            android.util.Log.d("RecipeViewModel", "配料数据加载成功: ${seasoningData.size} 个配料")
        } catch (e: Exception) {
            android.util.Log.e("RecipeViewModel", "加载配料数据失败: ${e.message}", e)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class RecipeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val randomRecipes: List<Recipe> = emptyList()
)