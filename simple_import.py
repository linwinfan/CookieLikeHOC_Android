#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import re
import json
import logging
from pathlib import Path
from typing import List, Dict, Optional
from dataclasses import dataclass, asdict
from datetime import datetime

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

@dataclass
class Recipe:
    title: str
    category: str
    description: str = ""
    difficulty: str = "未知"
    cooking_time: int = 0
    servings: int = 1
    ingredients: List[str] = None
    instructions: List[str] = None
    tips: str = ""
    nutrition: str = ""
    image_path: str = ""
    source_file: str = ""
    
    def __post_init__(self):
        if self.ingredients is None:
            self.ingredients = []
        if self.instructions is None:
            self.instructions = []

def parse_markdown_recipe(file_path: Path) -> Optional[Recipe]:
    """解析单个 Markdown 菜谱文件"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # 提取标题
        title_match = re.search(r'^#\s+(.+)$', content, re.MULTILINE)
        title = title_match.group(1).strip() if title_match else file_path.stem
        
        # 确定分类
        category_mapping = {
            '主食': 'staple', '炒菜': 'stir_fry', '炖菜': 'stew', '蒸菜': 'steam',
            '烤类': 'grill', '炸品': 'fried', '凉拌': 'cold_dish', '卤菜': 'braised',
            '早餐': 'breakfast', '汤': 'soup', '烫菜': 'blanched', '砂锅菜': 'casserole',
            '煮锅': 'hot_pot', '饮品': 'beverage', '配料': 'seasoning'
        }
        category_name = file_path.parent.name
        category = category_mapping.get(category_name, 'other')
        
        # 提取图片路径
        image_match = re.search(r'!\[.*?\]\((.+?)\)', content)
        image_path = image_match.group(1) if image_match else ""
        
        # 提取配料
        ingredients = []
        sections = ['## 配料', '## 品类']
        for section in sections:
            pattern = rf'{section}\s*\n(.*?)(?=\n##|\n#|\Z)'
            match = re.search(pattern, content, re.DOTALL)
            if match:
                ingredient_text = match.group(1).strip()
                lines = ingredient_text.split('\n')
                for line in lines:
                    line = line.strip()
                    if line.startswith('- '):
                        ingredient = line[2:].strip()
                        ingredient = re.sub(r'_.*?_', '', ingredient)
                        ingredient = re.sub(r'\(.*?\)', '', ingredient)
                        ingredient = ingredient.strip()
                        if ingredient and not ingredient.startswith('_'):
                            ingredients.append(ingredient)
                break
        
        # 提取步骤
        instructions = []
        pattern = r'## 步骤\s*\n(.*?)(?=\n##|\n#|\Z)'
        match = re.search(pattern, content, re.DOTALL)
        if match:
            steps_text = match.group(1).strip()
            lines = steps_text.split('\n')
            for line in lines:
                line = line.strip()
                if line.startswith('- '):
                    step = line[2:].strip()
                    step = re.sub(r'^\d+\.\s*', '', step)
                    if step:
                        instructions.append(step)
        
        # 估算烹饪时间
        cooking_time = 30  # 默认30分钟
        time_patterns = [r'(\d+)\s*分钟', r'(\d+)\s*小时']
        text = content + ' '.join(instructions)
        for pattern in time_patterns:
            matches = re.findall(pattern, text)
            for match in matches:
                time_val = int(match)
                if '小时' in pattern:
                    time_val *= 60
                cooking_time = max(cooking_time, time_val)
                break
        
        # 估算难度
        step_count = len(instructions)
        ingredient_count = len(ingredients)
        if step_count <= 2 and ingredient_count <= 3:
            difficulty = "简单"
        elif step_count <= 4 and ingredient_count <= 6:
            difficulty = "中等"
        else:
            difficulty = "困难"
        
        # 估算份数
        servings = 2  # 默认2人份
        for ingredient in ingredients:
            weight_match = re.search(r'(\d+)g', ingredient)
            if weight_match:
                weight = int(weight_match.group(1))
                if weight > 500:
                    servings = 4
                    break
        
        recipe = Recipe(
            title=title,
            category=category,
            description=f"{category_name}类菜品",
            difficulty=difficulty,
            cooking_time=cooking_time,
            servings=servings,
            ingredients=ingredients,
            instructions=instructions,
            image_path=image_path,
            source_file=str(file_path)
        )
        
        logger.info(f"成功解析菜谱: {title}")
        return recipe
        
    except Exception as e:
        logger.error(f"解析文件 {file_path} 时出错: {e}")
        return None

def import_all_recipes():
    """导入所有菜谱数据"""
    project_path = Path("e:/UGit/CookLikeHOC")
    
    if not project_path.exists():
        print(f"错误: 项目路径不存在 - {project_path}")
        return
    
    print("开始导入 CookLikeHOC 菜谱数据...")
    print(f"项目路径: {project_path}")
    
    recipes = []
    stats = {'total_files': 0, 'successful': 0, 'failed': 0, 'categories': {}}
    
    # 扫描所有分类目录
    categories = ['主食', '炒菜', '炖菜', '蒸菜', '烤类', '炸品', '凉拌', '卤菜', 
                 '早餐', '汤', '烫菜', '砂锅菜', '煮锅', '饮品', '配料']
    
    for category_name in categories:
        category_dir = project_path / category_name
        if category_dir.exists() and category_dir.is_dir():
            print(f"扫描分类目录: {category_name}")
            
            for file_path in category_dir.glob("*.md"):
                if file_path.name != "README.md":
                    stats['total_files'] += 1
                    recipe = parse_markdown_recipe(file_path)
                    
                    if recipe:
                        recipes.append(recipe)
                        stats['successful'] += 1
                        
                        category = recipe.category
                        if category not in stats['categories']:
                            stats['categories'][category] = 0
                        stats['categories'][category] += 1
                    else:
                        stats['failed'] += 1
    
    print(f"\n导入完成! 成功: {stats['successful']}, 失败: {stats['failed']}")
    
    # 导出为 JSON
    recipes_data = {
        'metadata': {
            'source': 'CookLikeHOC',
            'import_time': datetime.now().isoformat(),
            'total_recipes': len(recipes),
            'categories': list(stats['categories'].keys())
        },
        'recipes': [asdict(recipe) for recipe in recipes]
    }
    
    with open("cooklikehoc_recipes.json", 'w', encoding='utf-8') as f:
        json.dump(recipes_data, f, ensure_ascii=False, indent=2)
    
    print(f"数据已导出到: cooklikehoc_recipes.json")
    
    # 按分类导出
    os.makedirs("android_assets", exist_ok=True)
    
    # 按分类分组
    category_groups = {}
    for recipe in recipes:
        category = recipe.category
        if category not in category_groups:
            category_groups[category] = []
        category_groups[category].append(recipe)
    
    # 导出每个分类
    for category, category_recipes in category_groups.items():
        category_file = f"android_assets/{category}_recipes.json"
        category_data = {
            'category': category,
            'count': len(category_recipes),
            'recipes': [asdict(recipe) for recipe in category_recipes]
        }
        
        with open(category_file, 'w', encoding='utf-8') as f:
            json.dump(category_data, f, ensure_ascii=False, indent=2)
    
    # 创建索引文件
    index_data = {
        'total_recipes': len(recipes),
        'categories': {cat: len(recipes_list) for cat, recipes_list in category_groups.items()},
        'files': [f"{cat}_recipes.json" for cat in category_groups.keys()]
    }
    
    with open("android_assets/recipes_index.json", 'w', encoding='utf-8') as f:
        json.dump(index_data, f, ensure_ascii=False, indent=2)
    
    print("Android Assets 已导出到: android_assets/")
    
    # 打印摘要
    print("\n" + "="*60)
    print("🍳 CookLikeHOC 菜谱导入摘要")
    print("="*60)
    print(f"📊 总文件数: {stats['total_files']}")
    print(f"✅ 成功导入: {stats['successful']}")
    print(f"❌ 导入失败: {stats['failed']}")
    print(f"📈 成功率: {stats['successful']/stats['total_files']*100:.1f}%")
    
    print("\n📋 分类统计:")
    for category, count in stats['categories'].items():
        print(f"  {category}: {count} 个菜谱")
    
    print("\n🔥 热门菜谱预览:")
    for i, recipe in enumerate(recipes[:10]):
        print(f"  {i+1}. {recipe.title} ({recipe.category})")
    
    print("="*60)
    print("🎉 导入完成!")

if __name__ == "__main__":
    import_all_recipes()