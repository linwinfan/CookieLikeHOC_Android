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
fun SearchScreen(
    onRecipeClick: (Recipe) -> Unit,
    onBackClick: () -> Unit,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val allRecipes by viewModel.allRecipes.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 搜索栏
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    placeholder = { Text("搜索菜谱、配料...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "搜索"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.updateSearchQuery("") }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "清除"
                                )
                            }
                        }
                    }
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
        
        // 搜索结果
        when {
            searchQuery.isEmpty() -> {
                // 显示所有菜谱
                AllRecipesContent(
                    recipes = allRecipes,
                    onRecipeClick = onRecipeClick,
                    onFavoriteClick = { recipeId ->
                        viewModel.toggleFavorite(recipeId)
                    }
                )
            }
            searchResults.isEmpty() -> {
                // 无搜索结果
                NoResultsContent(query = searchQuery)
            }
            else -> {
                // 显示搜索结果
                SearchResultsContent(
                    results = searchResults,
                    query = searchQuery,
                    onRecipeClick = onRecipeClick,
                    onFavoriteClick = { recipeId ->
                        viewModel.toggleFavorite(recipeId)
                    }
                )
            }
        }
    }
}

@Composable
private fun AllRecipesContent(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "所有菜谱 (${recipes.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(recipes) { recipe ->
            RecipeCard(
                recipe = recipe,
                onClick = { onRecipeClick(recipe) },
                onFavoriteClick = { onFavoriteClick(recipe.id) }
            )
        }
    }
}

@Composable
private fun SearchResultsContent(
    results: List<Recipe>,
    query: String,
    onRecipeClick: (Recipe) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "搜索结果 (${results.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "关键词: \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        items(results) { recipe ->
            RecipeCard(
                recipe = recipe,
                onClick = { onRecipeClick(recipe) },
                onFavoriteClick = { onFavoriteClick(recipe.id) }
            )
        }
    }
}

@Composable
private fun NoResultsContent(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "未找到相关菜谱",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "尝试使用其他关键词搜索",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}