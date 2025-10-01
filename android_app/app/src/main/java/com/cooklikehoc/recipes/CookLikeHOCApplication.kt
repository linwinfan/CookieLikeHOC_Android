package com.cooklikehoc.recipes

import android.app.Application
import com.cooklikehoc.recipes.utils.DataImporter
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CookLikeHOCApplication : Application() {
    
    @Inject
    lateinit var dataImporter: DataImporter
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        // 在后台线程中导入数据
        applicationScope.launch {
            try {
                val result = dataImporter.importAllData(this@CookLikeHOCApplication)
                if (result.isSuccess) {
                    android.util.Log.d("CookLikeHOC", "数据导入成功: ${result.getOrNull()}")
                } else {
                    android.util.Log.e("CookLikeHOC", "数据导入失败: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("CookLikeHOC", "数据导入异常: ${e.message}")
            }
        }
    }
}