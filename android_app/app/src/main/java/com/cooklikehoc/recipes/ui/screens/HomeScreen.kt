package com.cooklikehoc.recipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import com.cooklikehoc.recipes.data.model.Recipe
import com.cooklikehoc.recipes.data.model.Category
import com.cooklikehoc.recipes.ui.components.RecipeCard
import com.cooklikehoc.recipes.ui.components.CategoryChip
import com.cooklikehoc.recipes.ui.viewmodel.RecipeViewModel
import com.cooklikehoc.recipes.ui.screens.CategorySection
import com.cooklikehoc.recipes.ui.screens.RecommendedSection
import com.cooklikehoc.recipes.ui.screens.RecentSection
import com.cooklikehoc.recipes.ui.screens.RandomSection
import com.cooklikehoc.recipes.ui.screens.SeasoningSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecipeClick: (Recipe) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val recommendedRecipes by viewModel.recommendedRecipes.collectAsStateWithLifecycle()
    val recentRecipes by viewModel.recentRecipes.collectAsStateWithLifecycle()
    val seasoningRecipes by viewModel.seasoningRecipes.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.getRandomRecipes(5)
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部应用栏
        TopAppBar(
            title = {
                Text(
                    text = "像老乡鸡那样做饭",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "搜索"
                    )
                }
            }
        )
        
        // 主要内容
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 分类横向滚动
            item {
                CategorySection(
                    categories = categories,
                    onCategoryClick = onCategoryClick
                )
            }
            
            // 推荐菜谱
            if (recommendedRecipes.isNotEmpty()) {
                item {
                    RecommendedSection(
                        recipes = recommendedRecipes,
                        onRecipeClick = onRecipeClick,
                        onFavoriteClick = { recipeId ->
                            viewModel.toggleFavorite(recipeId)
                        }
                    )
                }
            }
            
            // 最新菜谱
            if (recentRecipes.isNotEmpty()) {
                item {
                    RecentSection(
                        recipes = recentRecipes,
                        onRecipeClick = onRecipeClick,
                        onFavoriteClick = { recipeId ->
                            viewModel.toggleFavorite(recipeId)
                        }
                    )
                }
            }
            
            // 随机推荐
            if (uiState.randomRecipes.isNotEmpty()) {
                item {
                    RandomSection(
                        recipes = uiState.randomRecipes,
                        onRecipeClick = onRecipeClick,
                        onFavoriteClick = { recipeId ->
                            viewModel.toggleFavorite(recipeId)
                        },
                        onRefreshClick = {
                            viewModel.getRandomRecipes(5)
                        }
                    )
                }
            }
            
            // 配料栏目
            if (seasoningRecipes.isNotEmpty()) {
                item {
                    SeasoningSection(
                        seasonings = seasoningRecipes,
                        onSeasoningClick = onRecipeClick
                    )
                }
            }
        }
    }
    
    // 显示加载状态
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    
    // 显示错误信息
    uiState.error?.let { error ->
        LaunchedEffect(key1 = error) {
            // 这里可以显示 Snackbar 或其他错误提示
            viewModel.clearError()
        }
    }
}