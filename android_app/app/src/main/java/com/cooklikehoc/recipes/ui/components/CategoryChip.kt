package com.cooklikehoc.recipes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cooklikehoc.recipes.data.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val gradientColors = getCategoryGradient(category.id)
    val icon = getCategoryIcon(category.id)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category.displayName,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                
                if (category.recipeCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${category.recipeCount}个",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun getCategoryGradient(categoryId: String): List<Color> {
    return when (categoryId) {
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
private fun getCategoryIcon(categoryId: String): ImageVector {
    return when (categoryId) {
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