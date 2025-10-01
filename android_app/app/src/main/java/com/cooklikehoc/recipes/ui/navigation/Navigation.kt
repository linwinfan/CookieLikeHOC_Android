package com.cooklikehoc.recipes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.cooklikehoc.recipes.ui.screens.*

@Composable
fun CookLikeHOCNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // 主页
        composable("home") {
            HomeScreen(
                onRecipeClick = { recipe ->
                    navController.navigate("recipe_detail/${recipe.id}")
                },
                onCategoryClick = { categoryId ->
                    navController.navigate("category/$categoryId")
                },
                onSearchClick = {
                    navController.navigate("search")
                }
            )
        }
        
        // 菜谱详情
        composable(
            "recipe_detail/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
            RecipeDetailScreen(
                recipeId = recipeId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // 搜索页面
        composable("search") {
            SearchScreen(
                onRecipeClick = { recipe ->
                    navController.navigate("recipe_detail/${recipe.id}")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // 分类页面
        composable(
            "category/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            CategoryScreen(
                categoryId = categoryId,
                onRecipeClick = { recipe ->
                    navController.navigate("recipe_detail/${recipe.id}")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // 收藏页面
        composable("favorites") {
            FavoritesScreen(
                onRecipeClick = { recipe ->
                    navController.navigate("recipe_detail/${recipe.id}")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}