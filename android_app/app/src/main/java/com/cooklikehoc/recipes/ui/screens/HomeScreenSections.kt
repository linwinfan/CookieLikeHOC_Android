package com.cooklikehoc.recipes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cooklikehoc.recipes.data.model.Recipe
import com.cooklikehoc.recipes.data.model.Category
import com.cooklikehoc.recipes.ui.components.RecipeCard
import com.cooklikehoc.recipes.ui.components.CategoryChip

@Composable
fun CategorySection(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit
) {
    Column {
        Text(
            text = "菜谱分类",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(560.dp), // 固定高度以显示7行
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(categories) { category ->
                CategoryChip(
                    category = category,
                    onClick = { onCategoryClick(category.id) }
                )
            }
        }
    }
}

@Composable
fun SeasoningSection(
    seasonings: List<Recipe>,
    onSeasoningClick: (Recipe) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "菜谱配料",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            modifier = Modifier.height(300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(seasonings) { seasoning ->
                SeasoningCard(
                    seasoning = seasoning,
                    onClick = { onSeasoningClick(seasoning) }
                )
            }
        }
    }
}

@Composable
private fun SeasoningCard(
    seasoning: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 配料图标
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 配料信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = seasoning.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (seasoning.description.isNotBlank()) {
                    Text(
                        text = seasoning.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // 配料数量指示器
            if (seasoning.ingredients.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "${seasoning.ingredients.size}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendedSection(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    Column {
        Text(
            text = "推荐菜谱",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe) },
                    onFavoriteClick = { onFavoriteClick(recipe.id) },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
}

@Composable
fun RecentSection(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    Column {
        Text(
            text = "最新菜谱",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe) },
                    onFavoriteClick = { onFavoriteClick(recipe.id) },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
}

@Composable
fun RandomSection(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onRefreshClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "随机推荐",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "刷新推荐"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe) },
                    onFavoriteClick = { onFavoriteClick(recipe.id) },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
}