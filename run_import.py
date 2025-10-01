#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
CookLikeHOC 菜谱数据导入运行脚本
一键导入所有菜谱数据并生成各种格式的输出
"""

import os
import sys
import time
from pathlib import Path
from CookLikeHOCImporter import DataImporter, main as import_main
from android_importer import AndroidDataGenerator

def print_banner():
    """打印欢迎横幅"""
    banner = r"""
╔══════════════════════════════════════════════════════════════╗
║                    🍳 CookLikeHOC 菜谱导入器                    ║
║                                                              ║
║  从 e:\UGit\CookLikeHOC 导入所有菜谱数据                        ║
║  支持多种输出格式：JSON、Android Assets、数据库 Schema          ║
╚══════════════════════════════════════════════════════════════╝
"""
    print(banner)

def check_project_path():
    """检查项目路径是否存在"""
    project_path = r"e:\UGit\CookLikeHOC"
    if not os.path.exists(project_path):
        print(f"❌ 错误: 项目路径不存在 - {project_path}")
        print("请确保 CookLikeHOC 项目已正确下载到该路径")
        return False
    
    print(f"✅ 项目路径验证成功: {project_path}")
    return True

def run_full_import():
    """运行完整的导入流程"""
    print("\n🚀 开始完整导入流程...")
    
    try:
        # 1. 创建导入器并导入数据
        print("\n📖 步骤 1: 导入菜谱数据...")
        importer = DataImporter()
        stats = importer.import_all_recipes()
        
        if stats['successful'] == 0:
            print("❌ 没有成功导入任何菜谱，请检查项目路径和文件格式")
            return False
        
        # 2. 打印导入摘要
        importer.print_import_summary()
        
        # 3. 导出 JSON 格式
        print("\n📄 步骤 2: 导出 JSON 格式...")
        json_file = importer.export_to_json("cooklikehoc_recipes.json")
        print(f"✅ JSON 文件已生成: {json_file}")
        
        # 4. 导出 Android Assets
        print("\n📱 步骤 3: 导出 Android Assets...")
        assets_dir = importer.export_to_android_assets("android_assets")
        print(f"✅ Android Assets 已生成: {assets_dir}")
        
        # 5. 生成 Android 代码
        print("\n💻 步骤 4: 生成 Android 代码...")
        android_generator = AndroidDataGenerator(importer)
        code_dir = android_generator.generate_all_android_files("android_generated")
        print(f"✅ Android 代码已生成: {code_dir}")
        
        # 6. 生成使用说明
        generate_usage_guide(stats, json_file, assets_dir, code_dir)
        
        print("\n🎉 所有步骤完成!")
        return True
        
    except Exception as e:
        print(f"❌ 导入过程中发生错误: {e}")
        return False

def generate_usage_guide(stats, json_file, assets_dir, code_dir):
    """生成使用说明文档"""
    guide_content = f"""# CookLikeHOC 菜谱数据导入完成

## 📊 导入统计
- 总文件数: {stats['total_files']}
- 成功导入: {stats['successful']}
- 导入失败: {stats['failed']}
- 成功率: {stats['successful']/stats['total_files']*100:.1f}%

## 📁 生成的文件

### 1. JSON 数据文件
- **文件**: `{json_file}`
- **用途**: 通用的 JSON 格式菜谱数据
- **包含**: 所有菜谱的完整信息，包括元数据

### 2. Android Assets 目录
- **目录**: `{assets_dir}/`
- **用途**: Android 应用的 Assets 资源
- **文件结构**:
  ```
  {assets_dir}/
  ├── recipes_index.json          # 索引文件
  ├── staple_recipes.json         # 主食类菜谱
  ├── stir_fry_recipes.json       # 炒菜类菜谱
  ├── stew_recipes.json           # 炖菜类菜谱
  └── ...                         # 其他分类
  ```

### 3. Android 代码文件
- **目录**: `{code_dir}/`
- **文件**:
  - `DataModels.kt`: Room 数据库模型和 DAO
  - `ImporterActivity.kt`: 数据导入 Activity
  - `database_schema.sql`: 数据库建表语句

## 🔧 Android 集成步骤

### 1. 复制 Assets 文件
将 `{assets_dir}/` 目录中的所有 JSON 文件复制到 Android 项目的 `src/main/assets/` 目录

### 2. 添加依赖
在 `build.gradle` 中添加：
```gradle
dependencies {{
    implementation "androidx.room:room-runtime:2.4.3"
    implementation "androidx.room:room-ktx:2.4.3"
    kapt "androidx.room:room-compiler:2.4.3"
    implementation "com.google.code.gson:gson:2.10.1"
}}
```

### 3. 集成代码
1. 将 `DataModels.kt` 中的代码添加到项目中
2. 将 `ImporterActivity.kt` 作为导入功能的 Activity
3. 在 `AndroidManifest.xml` 中注册 Activity

### 4. 初始化数据
```kotlin
// 在应用启动时导入数据
val importer = ImporterActivity()
importer.importFromAssets()
```

## 📋 分类统计
"""
    
    for category, count in stats['categories'].items():
        guide_content += f"- {category}: {count} 个菜谱\n"
    
    guide_content += f"""
## 🎯 使用建议

1. **开发阶段**: 使用 JSON 文件进行数据验证和测试
2. **生产环境**: 使用 Android Assets 进行应用内数据导入
3. **数据库**: 使用提供的 Schema 创建 Room 数据库
4. **扩展**: 可以基于生成的代码进行功能扩展

## 📞 技术支持

如有问题，请检查：
1. 项目路径是否正确
2. 文件权限是否充足
3. 数据格式是否符合预期

---
生成时间: {time.strftime('%Y-%m-%d %H:%M:%S')}
"""
    
    with open("USAGE_GUIDE.md", 'w', encoding='utf-8') as f:
        f.write(guide_content)
    
    print("✅ 使用说明已生成: USAGE_GUIDE.md")

def main():
    """主函数"""
    print_banner()
    
    # 检查项目路径
    if not check_project_path():
        return False
    
    # 运行完整导入
    success = run_full_import()
    
    if success:
        print("\n" + "="*60)
        print("🎉 CookLikeHOC 菜谱数据导入完成!")
        print("📖 请查看 USAGE_GUIDE.md 了解如何使用生成的文件")
        print("="*60)
    else:
        print("\n❌ 导入失败，请检查错误信息并重试")
    
    return success

if __name__ == "__main__":
    main()