package com.cooklikehoc.recipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.cooklikehoc.recipes.data.model.Recipe
import com.cooklikehoc.recipes.ui.components.RecipeCard
import com.cooklikehoc.recipes.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryId: String,
    onRecipeClick: (Recipe) -> Unit,
    onBackClick: () -> Unit,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val categoryName = categories.find { it.id == categoryId }?.displayName ?: "未知菜谱分类"
    
    // 使用remember来缓存Flow，避免重复创建
    val categoryRecipesFlow = remember(categoryId) {
        viewModel.getCategoryRecipes(categoryId)
    }
    val categoryRecipes by categoryRecipesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    
    // 调试日志 - 只在categoryId变化时记录
    LaunchedEffect(categoryId) {
        android.util.Log.d("CategoryScreen", "分类ID: $categoryId")
        android.util.Log.d("CategoryScreen", "分类名称: $categoryName")
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部应用栏
        TopAppBar(
            title = {
                Text(
                    text = categoryName,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        )
        
        // 内容
        if (categoryRecipes.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.RestaurantMenu,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无${categoryName}菜谱",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "敬请期待更多精彩内容",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // 菜谱列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "${categoryName} (${categoryRecipes.size}道菜)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(categoryRecipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) },
                        onFavoriteClick = { viewModel.toggleFavorite(recipe.id) },
                        showCategory = false
                    )
                }
            }
        }
    }
}