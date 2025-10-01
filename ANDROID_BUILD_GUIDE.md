# 🚀 CookLikeHOC Android 应用构建指南

## 📱 项目概述

CookLikeHOC 是一个现代化的 Android 美食应用，基于从 CookLikeHOC 项目导入的 198 个精选菜谱数据构建。

### ✨ 核心特性

- **🎨 现代化 UI**: 使用 Jetpack Compose 和 Material Design 3
- **📊 丰富数据**: 198 个菜谱，15 个分类，完整的配料和步骤
- **🔍 智能搜索**: 支持菜谱名称、配料、分类搜索
- **❤️ 收藏功能**: 本地收藏管理
- **⭐ 评分系统**: 用户可以为菜谱评分和评论
- **📱 响应式设计**: 适配不同屏幕尺寸

## 🏗️ 技术架构

### 核心技术栈
- **UI**: Jetpack Compose + Material Design 3
- **架构**: MVVM + Repository Pattern
- **依赖注入**: Hilt
- **数据库**: Room
- **图片加载**: Coil
- **导航**: Navigation Compose
- **异步处理**: Kotlin Coroutines + Flow

### 项目结构
```
android_app/
├── app/
│   ├── build.gradle                 # 应用级构建配置
│   ├── src/main/
│   │   ├── java/com/cooklikehoc/recipes/
│   │   │   ├── data/                # 数据层
│   │   │   │   ├── model/           # 数据模型
│   │   │   │   ├── database/        # Room 数据库
│   │   │   │   └── repository/      # 数据仓库
│   │   │   ├── di/                  # 依赖注入
│   │   │   ├── ui/                  # UI 层
│   │   │   │   ├── components/      # 可复用组件
│   │   │   │   ├── screens/         # 页面
│   │   │   │   ├── theme/           # 主题配置
│   │   │   │   ├── viewmodel/       # ViewModel
│   │   │   │   └── navigation/      # 导航配置
│   │   │   ├── utils/               # 工具类
│   │   │   ├── MainActivity.kt      # 主 Activity
│   │   │   └── CookLikeHOCApplication.kt
│   │   ├── res/                     # 资源文件
│   │   │   ├── values/              # 字符串、颜色、主题
│   │   │   └── ...
│   │   ├── assets/                  # 菜谱数据文件
│   │   │   ├── cooklikehoc_recipes.json
│   │   │   └── android_assets/      # 分类数据
│   │   └── AndroidManifest.xml
│   └── ...
├── build.gradle                     # 项目级构建配置
├── settings.gradle                  # 项目设置
└── gradle.properties               # Gradle 属性
```

## 🛠️ 构建步骤

### 1. 准备数据文件

首先运行数据复制脚本：

```bash
python copy_assets.py
```

这将把所有菜谱数据文件复制到 Android 项目的 assets 目录。

### 2. 在 Android Studio 中打开项目

1. 启动 Android Studio
2. 选择 "Open an Existing Project"
3. 导航到 `android_app` 目录并选择

### 3. 同步项目

Android Studio 会自动开始 Gradle 同步。如果没有，请：
1. 点击 "Sync Now" 通知
2. 或者选择 File → Sync Project with Gradle Files

### 4. 配置构建环境

确保你的开发环境满足以下要求：
- **Android Studio**: Arctic Fox 或更新版本
- **Compile SDK**: 34
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34
- **Java**: JDK 11 或更新版本
- **Kotlin**: 1.9.10

### 5. 构建应用

#### Debug 构建
```bash
./gradlew assembleDebug
```

#### Release 构建
```bash
./gradlew assembleRelease
```

### 6. 运行应用

1. 连接 Android 设备或启动模拟器
2. 点击 Android Studio 中的 "Run" 按钮
3. 或使用命令行：`./gradlew installDebug`

## 📊 数据结构

### 菜谱数据模型
```kotlin
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: Long,
    val title: String,              // 菜谱名称
    val category: String,           // 分类
    val ingredients: List<String>,  // 配料列表
    val instructions: List<String>, // 制作步骤
    val imagePath: String,          // 图片路径
    val difficulty: String,         // 难度等级
    val cookingTime: Int,          // 制作时间（分钟）
    val servings: Int,             // 份数
    val tips: String,              // 小贴士
    val rating: Float,             // 评分
    val isFavorite: Boolean,       // 是否收藏
    val createdAt: Long,           // 创建时间
    val updatedAt: Long            // 更新时间
)
```

### 分类数据
应用支持以下 15 个菜谱分类：
- 主食 (49个菜谱)
- 炒菜 (29个菜谱)
- 炖菜 (21个菜谱)
- 蒸菜 (18个菜谱)
- 早餐、饮品、汤、甜品等

## 🎨 UI 设计

### 主要页面
1. **主页**: 分类浏览、推荐菜谱、随机推荐
2. **搜索页**: 全文搜索、筛选功能
3. **菜谱详情**: 完整的制作指南、评分功能
4. **分类页**: 按分类浏览菜谱
5. **收藏页**: 管理收藏的菜谱

### 设计特色
- **Material Design 3**: 现代化的设计语言
- **动态主题**: 支持浅色/深色模式
- **响应式布局**: 适配手机和平板
- **流畅动画**: 页面切换和交互动画

## 🔧 自定义配置

### 修改应用名称和图标
1. 编辑 `app/src/main/res/values/strings.xml` 中的 `app_name`
2. 替换 `app/src/main/res/mipmap/` 中的图标文件

### 修改主题颜色
编辑 `app/src/main/res/values/colors.xml` 中的颜色值

### 添加新功能
1. 在相应的包中添加新的 Kotlin 文件
2. 更新导航配置 `ui/navigation/Navigation.kt`
3. 如需要，更新数据模型和数据库

## 🚀 发布准备

### 1. 生成签名密钥
```bash
keytool -genkey -v -keystore cooklikehoc-release-key.keystore -alias cooklikehoc -keyalg RSA -keysize 2048 -validity 10000
```

### 2. 配置签名
在 `app/build.gradle` 中添加签名配置：
```gradle
android {
    signingConfigs {
        release {
            storeFile file('cooklikehoc-release-key.keystore')
            storePassword 'your_store_password'
            keyAlias 'cooklikehoc'
            keyPassword 'your_key_password'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            // ... 其他配置
        }
    }
}
```

### 3. 构建发布版本
```bash
./gradlew assembleRelease
```

## 📱 功能演示

### 主要功能流程
1. **启动应用** → 显示启动屏幕 → 进入主页
2. **浏览菜谱** → 选择分类 → 查看菜谱列表 → 点击查看详情
3. **搜索菜谱** → 输入关键词 → 查看搜索结果
4. **收藏管理** → 点击收藏按钮 → 在收藏页面管理
5. **评分功能** → 在详情页点击评分 → 提交评分和评论

## 🐛 常见问题

### 构建失败
1. 检查 Android Studio 版本和 SDK 配置
2. 清理项目：Build → Clean Project
3. 重新同步：File → Sync Project with Gradle Files

### 数据加载问题
1. 确保 assets 目录中有菜谱数据文件
2. 检查 DataImporter 的实现
3. 查看 Logcat 中的错误信息

### UI 显示问题
1. 检查 Compose 版本兼容性
2. 确保主题配置正确
3. 验证图片路径是否正确

## 📞 技术支持

如果在构建过程中遇到问题，请检查：
1. **Gradle 版本兼容性**
2. **依赖库版本**
3. **Android SDK 配置**
4. **设备/模拟器兼容性**

---

🎉 **恭喜！** 你现在拥有了一个功能完整的 Android 美食应用！

这个应用不仅包含了丰富的菜谱数据，还具备现代化的用户界面和完整的功能体验。你可以根据需要进一步定制和扩展功能。