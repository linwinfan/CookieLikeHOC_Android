package com.cooklikehoc.recipes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cooklikehoc.recipes.data.model.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCategory: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // 图片区域
            RecipeImageSection(
                recipe = recipe,
                onFavoriteClick = onFavoriteClick
            )
            
            // 内容区域
            RecipeContentSection(
                recipe = recipe,
                showCategory = showCategory
            )
        }
    }
}

@Composable
private fun RecipePlaceholderImage(
    recipe: Recipe,
    modifier: Modifier = Modifier
) {
    val gradientColors = getCategoryGradientForRecipe(recipe.category)
    val icon = getCategoryIconForRecipe(recipe.category)
    
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(gradientColors)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = recipe.title,
                modifier = Modifier.size(48.dp),
                tint = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = recipe.getCategoryDisplayName(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getImagePath(recipe: Recipe): String? {
    android.util.Log.d("RecipeCard", "原始图片路径: '${recipe.imagePath}'")
    
    return when {
        recipe.imagePath.isBlank() -> {
            android.util.Log.d("RecipeCard", "图片路径为空，使用占位图")
            null
        }
        recipe.imagePath.startsWith("../images/") -> {
            // 处理相对路径，转换为assets路径
            val fileName = recipe.imagePath.removePrefix("../images/")
            val assetPath = "file:///android_asset/images/$fileName"
            android.util.Log.d("RecipeCard", "转换后的assets路径: '$assetPath'")
            assetPath
        }
        recipe.imagePath.startsWith("images/") -> {
            val assetPath = "file:///android_asset/${recipe.imagePath}"
            android.util.Log.d("RecipeCard", "直接assets路径: '$assetPath'")
            assetPath
        }
        recipe.imagePath.contains("../") -> {
            android.util.Log.d("RecipeCard", "包含相对路径，使用占位图")
            null // 其他相对路径使用占位图
        }
        else -> {
            // 假设是文件名，在images目录中查找
            val assetPath = "file:///android_asset/images/${recipe.imagePath}"
            android.util.Log.d("RecipeCard", "假设文件名，assets路径: '$assetPath'")
            assetPath
        }
    }
}

@Composable
private fun getCategoryGradientForRecipe(category: String): List<Color> {
    return when (category) {
        "staple" -> listOf(Color(0xFF6B73FF), Color(0xFF9B59B6))
        "stir_fry" -> listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
        "stew" -> listOf(Color(0xFF4ECDC4), Color(0xFF44A08D))
        "steam" -> listOf(Color(0xFF74B9FF), Color(0xFF0984E3))
        "grill" -> listOf(Color(0xFFFF7675), Color(0xFFD63031))
        "fried" -> listOf(Color(0xFFFDCB6E), Color(0xFFE17055))
        "cold_dish" -> listOf(Color(0xFF81ECEC), Color(0xFF00B894))
        "braised" -> listOf(Color(0xFFA29BFE), Color(0xFF6C5CE7))
        "breakfast" -> listOf(Color(0xFFFFB8B8), Color(0xFFFF6B9D))
        "soup" -> listOf(Color(0xFF74B9FF), Color(0xFF0984E3))
        "blanched" -> listOf(Color(0xFF55A3FF), Color(0xFF003D82))
        "casserole" -> listOf(Color(0xFFFF9F43), Color(0xFFFF6B35))
        "hot_pot" -> listOf(Color(0xFFFF6B6B), Color(0xFFEE5A24))
        "beverage" -> listOf(Color(0xFF00CEC9), Color(0xFF00B894))
        "seasoning" -> listOf(Color(0xFFFFB8B8), Color(0xFFFF6B9D))
        else -> listOf(Color(0xFF74B9FF), Color(0xFF0984E3))
    }
}

@Composable
private fun getCategoryIconForRecipe(category: String): ImageVector {
    return when (category) {
        "staple" -> Icons.Filled.Restaurant
        "stir_fry" -> Icons.Filled.LocalFireDepartment
        "stew" -> Icons.Filled.Restaurant
        "steam" -> Icons.Filled.Cloud
        "grill" -> Icons.Filled.Whatshot
        "fried" -> Icons.Filled.LocalFireDepartment
        "cold_dish" -> Icons.Filled.AcUnit
        "braised" -> Icons.Filled.Restaurant
        "breakfast" -> Icons.Filled.WbSunny
        "soup" -> Icons.Filled.LocalCafe
        "blanched" -> Icons.Filled.Opacity
        "casserole" -> Icons.Filled.Restaurant
        "hot_pot" -> Icons.Filled.Whatshot
        "beverage" -> Icons.Filled.LocalCafe
        "seasoning" -> Icons.Filled.Star
        else -> Icons.Filled.Restaurant
    }
}

@Composable
private fun RecipeImageSection(
    recipe: Recipe,
    onFavoriteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val imagePath = getImagePath(recipe)
            android.util.Log.d("RecipeCard", "为菜谱 '${recipe.title}' 加载图片: '$imagePath'")
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onSuccess = { 
                    android.util.Log.d("RecipeCard", "图片加载成功: '${recipe.title}'")
                },
                onError = { error ->
                    android.util.Log.e("RecipeCard", "图片加载失败: '${recipe.title}', 错误: ${error.result.throwable?.message}")
                }
            )
            
            // 如果没有图片路径，显示美观的占位图
            if (getImagePath(recipe) == null) {
                android.util.Log.d("RecipeCard", "显示占位图: '${recipe.title}'")
                RecipePlaceholderImage(
                    recipe = recipe,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // 收藏按钮
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(
                    Color.Black.copy(alpha = 0.3f),
                    RoundedCornerShape(50)
                )
        ) {
            Icon(
                imageVector = if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (recipe.isFavorite) "取消收藏" else "收藏",
                tint = if (recipe.isFavorite) Color.Red else Color.White
            )
        }
        
        // 难度标签已移除 - 原始数据中没有difficulty字段
    }
}