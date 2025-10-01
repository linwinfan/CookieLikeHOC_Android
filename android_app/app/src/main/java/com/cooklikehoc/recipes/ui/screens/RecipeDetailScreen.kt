package com.cooklikehoc.recipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cooklikehoc.recipes.data.model.Recipe
import com.cooklikehoc.recipes.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    onBackClick: () -> Unit,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    var recipe by remember { mutableStateOf<Recipe?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(recipeId) {
        recipe = viewModel.getRecipeById(recipeId)
        isFavorite = viewModel.isRecipeFavorite(recipeId)
    }
    
    recipe?.let { currentRecipe ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部应用栏
            TopAppBar(
                title = { Text(currentRecipe.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            viewModel.toggleFavorite(recipeId)
                            isFavorite = !isFavorite
                        }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "取消收藏" else "收藏",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showRatingDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "评分"
                        )
                    }
                }
            )
            
            // 内容
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 图片
                item {
                    RecipeImageHeader(recipe = currentRecipe)
                }
                
                // 基本信息
                item {
                    RecipeBasicInfo(recipe = currentRecipe)
                }
                
                // 配料
                item {
                    RecipeIngredients(ingredients = currentRecipe.ingredients)
                }
                
                // 制作步骤
                item {
                    RecipeInstructions(instructions = currentRecipe.instructions)
                }
                

            }
        }
        
        // 评分对话框
        if (showRatingDialog) {
            RatingDialog(
                onDismiss = { showRatingDialog = false },
                onRatingSubmit = { rating, comment ->
                    viewModel.rateRecipe(recipeId, rating, comment)
                    showRatingDialog = false
                }
            )
        }
    }
}

@Composable
private fun RecipeImageHeader(recipe: Recipe) {
    val imagePath = getImagePath(recipe)
    android.util.Log.d("RecipeDetailScreen", "详情页面图片路径: '${recipe.imagePath}' -> '$imagePath'")
    
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imagePath)
            .crossfade(true)
            .build(),
        contentDescription = recipe.title,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop,
        onSuccess = { 
            android.util.Log.d("RecipeDetailScreen", "详情页面图片加载成功: '${recipe.title}'")
        },
        onError = { error ->
            android.util.Log.e("RecipeDetailScreen", "详情页面图片加载失败: '${recipe.title}', 错误: ${error.result.throwable?.message}")
        }
    )
}

private fun getImagePath(recipe: Recipe): String? {
    return when {
        recipe.imagePath.isBlank() -> null
        recipe.imagePath.startsWith("../images/") -> {
            // 处理相对路径，转换为assets路径
            val fileName = recipe.imagePath.removePrefix("../images/")
            "file:///android_asset/images/$fileName"
        }
        recipe.imagePath.startsWith("images/") -> {
            "file:///android_asset/${recipe.imagePath}"
        }
        recipe.imagePath.contains("../") -> null // 其他相对路径使用占位图
        else -> {
            // 假设是文件名，在images目录中查找
            "file:///android_asset/images/${recipe.imagePath}"
        }
    }
}

@Composable
private fun RecipeBasicInfo(recipe: Recipe) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "基本信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoItem(
                    icon = Icons.Filled.Category,
                    label = "菜谱分类",
                    value = recipe.getCategoryDisplayName()
                )
                InfoItem(
                    icon = Icons.Filled.List,
                    label = "配料数量",
                    value = "${recipe.ingredients.size}种"
                )
                InfoItem(
                    icon = Icons.Filled.Assignment,
                    label = "制作步骤",
                    value = "${recipe.instructions.size}步"
                )
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}