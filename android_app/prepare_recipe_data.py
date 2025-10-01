#!/usr/bin/env python3
"""
CookLikeHOC Recipe Data Preparation
将采集到的菜谱数据整合并准备用于Android应用
"""

import json
import os
import shutil
from datetime import datetime

def load_category_recipes(category_file):
    """加载单个分类的菜谱数据"""
    try:
        with open(f'../android_assets/{category_file}', 'r', encoding='utf-8') as f:
            data = json.load(f)
            return data.get('recipes', [])
    except Exception as e:
        print(f"加载 {category_file} 失败: {e}")
        return []

def prepare_recipe_data():
    """准备菜谱数据"""
    print("CookLikeHOC Recipe Data Preparation")
    print("=" * 50)
    
    # 读取索引文件
    try:
        with open('../android_assets/recipes_index.json', 'r', encoding='utf-8') as f:
            index_data = json.load(f)
    except Exception as e:
        print(f"读取索引文件失败: {e}")
        return
    
    # 合并所有菜谱数据
    all_recipes = []
    categories_data = []
    
    category_mapping = {
        "staple": "主食",
        "stir_fry": "炒菜", 
        "stew": "炖菜",
        "steam": "蒸菜",
        "grill": "烤类",
        "fried": "炸品",
        "cold_dish": "凉拌",
        "braised": "卤菜",
        "breakfast": "早餐",
        "soup": "汤",
        "blanched": "烫菜",
        "casserole": "砂锅菜",
        "hot_pot": "煮锅",
        "beverage": "饮品",
        "seasoning": "配料"
    }
    
    recipe_id = 1
    
    for category_id, count in index_data['categories'].items():
        category_file = f"{category_id}_recipes.json"
        print(f"处理分类: {category_mapping.get(category_id, category_id)} ({count}个菜谱)")
        
        # 加载分类菜谱
        recipes = load_category_recipes(category_file)
        
        # 处理每个菜谱
        processed_recipes = []
        for recipe in recipes:
            # 添加ID和标准化数据
            processed_recipe = {
                "id": recipe_id,
                "title": recipe.get("title", ""),
                "category": category_id,
                "description": recipe.get("description", ""),
                "difficulty": recipe.get("difficulty", "未知"),
                "cooking_time": recipe.get("cooking_time", 0),
                "servings": recipe.get("servings", 1),
                "ingredients": recipe.get("ingredients", []),
                "instructions": recipe.get("instructions", []),
                "tips": recipe.get("tips", ""),
                "nutrition": recipe.get("nutrition", ""),
                "image_path": recipe.get("image_path", ""),
                "source_file": category_file,
                "is_favorite": False,
                "rating": 0.0,
                "created_at": datetime.now().isoformat(),
                "updated_at": datetime.now().isoformat()
            }
            processed_recipes.append(processed_recipe)
            all_recipes.append(processed_recipe)
            recipe_id += 1
        
        # 创建分类数据
        category_data = {
            "id": category_id,
            "name": category_id,
            "display_name": category_mapping.get(category_id, category_id),
            "description": f"{category_mapping.get(category_id, category_id)}类菜品",
            "icon": "",
            "sort_order": len(categories_data),
            "recipe_count": len(processed_recipes)
        }
        categories_data.append(category_data)
    
    print(f"\n总计处理: {len(all_recipes)} 个菜谱")
    print(f"分类数量: {len(categories_data)} 个")
    
    # 创建assets目录
    assets_dir = "app/src/main/assets"
    os.makedirs(assets_dir, exist_ok=True)
    
    # 保存合并的菜谱数据
    recipes_file = os.path.join(assets_dir, "cooklikehoc_recipes.json")
    with open(recipes_file, 'w', encoding='utf-8') as f:
        json.dump(all_recipes, f, ensure_ascii=False, indent=2)
    print(f"✅ 保存菜谱数据: {recipes_file}")
    
    # 保存分类数据
    categories_file = os.path.join(assets_dir, "categories.json")
    with open(categories_file, 'w', encoding='utf-8') as f:
        json.dump(categories_data, f, ensure_ascii=False, indent=2)
    print(f"✅ 保存分类数据: {categories_file}")
    
    # 保存元数据
    metadata = {
        "source": "CookLikeHOC Recipe Collection",
        "import_time": datetime.now().isoformat(),
        "total_recipes": len(all_recipes),
        "total_categories": len(categories_data),
        "categories": list(index_data['categories'].keys()),
        "version": "1.0.0"
    }
    
    metadata_file = os.path.join(assets_dir, "metadata.json")
    with open(metadata_file, 'w', encoding='utf-8') as f:
        json.dump(metadata, f, ensure_ascii=False, indent=2)
    print(f"✅ 保存元数据: {metadata_file}")
    
    # 复制分类文件到assets（可选，用于按需加载）
    category_assets_dir = os.path.join(assets_dir, "categories")
    os.makedirs(category_assets_dir, exist_ok=True)
    
    for category_file in index_data['files']:
        src_file = f"../android_assets/{category_file}"
        dst_file = os.path.join(category_assets_dir, category_file)
        if os.path.exists(src_file):
            shutil.copy2(src_file, dst_file)
            print(f"✅ 复制分类文件: {category_file}")
    
    print(f"\n🎉 数据准备完成！")
    print(f"📁 文件位置: {assets_dir}")
    print(f"📊 统计信息:")
    for category_id, count in index_data['categories'].items():
        display_name = category_mapping.get(category_id, category_id)
        print(f"   - {display_name}: {count}个菜谱")

if __name__ == '__main__':
    prepare_recipe_data()