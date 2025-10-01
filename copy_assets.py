#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
将导入的菜谱数据复制到 Android 项目的 assets 目录
"""

import os
import shutil
import json

def copy_assets_to_android():
    """复制资源文件到 Android 项目"""
    
    # 源目录和目标目录
    source_dir = "."
    android_assets_dir = "android_app/app/src/main/assets"
    
    # 创建 assets 目录
    os.makedirs(android_assets_dir, exist_ok=True)
    
    print("🚀 开始复制资源文件到 Android 项目...")
    
    # 复制主菜谱数据文件
    main_recipe_file = "cooklikehoc_recipes.json"
    if os.path.exists(main_recipe_file):
        shutil.copy2(main_recipe_file, android_assets_dir)
        print(f"✅ 已复制: {main_recipe_file}")
    else:
        print(f"❌ 文件不存在: {main_recipe_file}")
    
    # 复制分类数据文件
    android_assets_source = "android_assets"
    android_assets_target = os.path.join(android_assets_dir, "android_assets")
    
    if os.path.exists(android_assets_source):
        if os.path.exists(android_assets_target):
            shutil.rmtree(android_assets_target)
        shutil.copytree(android_assets_source, android_assets_target)
        print(f"✅ 已复制目录: {android_assets_source}")
        
        # 统计文件数量
        file_count = len([f for f in os.listdir(android_assets_target) if f.endswith('.json')])
        print(f"📊 共复制了 {file_count} 个分类数据文件")
    else:
        print(f"❌ 目录不存在: {android_assets_source}")
    
    # 验证复制结果
    print("\n📋 Android Assets 目录结构:")
    for root, dirs, files in os.walk(android_assets_dir):
        level = root.replace(android_assets_dir, '').count(os.sep)
        indent = ' ' * 2 * level
        print(f"{indent}{os.path.basename(root)}/")
        subindent = ' ' * 2 * (level + 1)
        for file in files:
            print(f"{subindent}{file}")
    
    print("\n🎉 资源文件复制完成！")
    print("💡 现在可以在 Android Studio 中打开项目并构建应用了。")

if __name__ == "__main__":
    copy_assets_to_android()