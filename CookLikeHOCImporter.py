#!/usr/bin/env python3
# -*- coding: utf-8 -*-
r"""
CookLikeHOC 菜谱数据导入器
从 e:\UGit\CookLikeHOC 目录导入所有菜谱数据
"""

import os
import re
import json
import logging
from pathlib import Path
from typing import List, Dict, Optional, Tuple
from dataclasses import dataclass, asdict
from datetime import datetime

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('import_log.txt', encoding='utf-8'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

@dataclass
class Recipe:
    """菜谱数据模型"""
    title: str
    category: str
    description: str = ""
    difficulty: str = "未知"
    cooking_time: int = 0  # 分钟
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

class CookLikeHOCParser:
    """CookLikeHOC 项目专用解析器"""
    
    def __init__(self, project_path: str):
        self.project_path = Path(project_path)
        self.categories = {
            '主食': 'staple',
            '炒菜': 'stir_fry', 
            '炖菜': 'stew',
            '蒸菜': 'steam',
            '烤类': 'grill',
            '炸品': 'fried',
            '凉拌': 'cold_dish',
            '卤菜': 'braised',
            '早餐': 'breakfast',
            '汤': 'soup',
            '烫菜': 'blanched',
            '砂锅菜': 'casserole',
            '煮锅': 'hot_pot',
            '饮品': 'beverage',
            '配料': 'seasoning'
        }
        
    def discover_recipe_files(self) -> List[Path]:
        """发现所有菜谱文件"""
        recipe_files = []
        
        for category_dir in self.project_path.iterdir():
            if category_dir.is_dir() and category_dir.name in self.categories:
                logger.info(f"扫描分类目录: {category_dir.name}")
                
                for file_path in category_dir.glob("*.md"):
                    if file_path.name != "README.md":
                        recipe_files.append(file_path)
                        logger.debug(f"发现菜谱文件: {file_path}")
        
        logger.info(f"总共发现 {len(recipe_files)} 个菜谱文件")
        return recipe_files
    
    def parse_markdown_recipe(self, file_path: Path) -> Optional[Recipe]:
        """解析单个 Markdown 菜谱文件"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # 提取标题
            title_match = re.search(r'^#\s+(.+)$', content, re.MULTILINE)
            title = title_match.group(1).strip() if title_match else file_path.stem
            
            # 确定分类
            category_name = file_path.parent.name
            category = self.categories.get(category_name, 'other')
            
            # 提取图片路径
            image_match = re.search(r'!\[.*?\]\((.+?)\)', content)
            image_path = image_match.group(1) if image_match else ""
            
            # 提取配料/品类
            ingredients = self._extract_ingredients(content)
            
            # 提取步骤
            instructions = self._extract_instructions(content)
            
            # 估算烹饪时间
            cooking_time = self._estimate_cooking_time(content, instructions)
            
            # 估算难度
            difficulty = self._estimate_difficulty(instructions, ingredients)
            
            # 估算份数
            servings = self._estimate_servings(content, ingredients)
            
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
            
            logger.debug(f"成功解析菜谱: {title}")
            return recipe
            
        except Exception as e:
            logger.error(f"解析文件 {file_path} 时出错: {e}")
            return None
    
    def _extract_ingredients(self, content: str) -> List[str]:
        """提取配料列表"""
        ingredients = []
        
        # 查找配料或品类部分
        sections = ['## 配料', '## 品类']
        for section in sections:
            pattern = rf'{section}\s*\n(.*?)(?=\n##|\n#|\Z)'
            match = re.search(pattern, content, re.DOTALL)
            
            if match:
                ingredient_text = match.group(1).strip()
                # 提取列表项
                lines = ingredient_text.split('\n')
                for line in lines:
                    line = line.strip()
                    if line.startswith('- '):
                        ingredient = line[2:].strip()
                        # 清理特殊标记
                        ingredient = re.sub(r'_.*?_', '', ingredient)  # 移除斜体
                        ingredient = re.sub(r'\(.*?\)', '', ingredient)  # 移除括号内容
                        ingredient = ingredient.strip()
                        if ingredient and not ingredient.startswith('_'):
                            ingredients.append(ingredient)
                break
        
        return ingredients
    
    def _extract_instructions(self, content: str) -> List[str]:
        """提取制作步骤"""
        instructions = []
        
        # 查找步骤部分
        pattern = r'## 步骤\s*\n(.*?)(?=\n##|\n#|\Z)'
        match = re.search(pattern, content, re.DOTALL)
        
        if match:
            steps_text = match.group(1).strip()
            lines = steps_text.split('\n')
            
            for line in lines:
                line = line.strip()
                if line.startswith('- '):
                    step = line[2:].strip()
                    # 清理步骤编号
                    step = re.sub(r'^\d+\.\s*', '', step)
                    if step:
                        instructions.append(step)
        
        return instructions
    
    def _estimate_cooking_time(self, content: str, instructions: List[str]) -> int:
        """估算烹饪时间（分钟）"""
        time_patterns = [
            r'(\d+)\s*分钟',
            r'(\d+)\s*小时',
            r'蒸制?\s*(\d+)\s*分钟',
            r'煮\s*(\d+)\s*分钟',
            r'炒\s*(\d+)\s*秒'
        ]
        
        total_time = 0
        text = content + ' '.join(instructions)
        
        for pattern in time_patterns:
            matches = re.findall(pattern, text)
            for match in matches:
                time_val = int(match)
                if '小时' in pattern:
                    time_val *= 60
                elif '秒' in pattern:
                    time_val = max(1, time_val // 60)  # 转换为分钟
                total_time += time_val
        
        # 如果没有找到时间信息，根据步骤数量估算
        if total_time == 0:
            step_count = len(instructions)
            if step_count <= 2:
                total_time = 15
            elif step_count <= 4:
                total_time = 30
            else:
                total_time = 45
        
        return min(total_time, 180)  # 最大3小时
    
    def _estimate_difficulty(self, instructions: List[str], ingredients: List[str]) -> str:
        """估算难度等级"""
        complexity_score = 0
        
        # 根据步骤数量
        step_count = len(instructions)
        if step_count <= 2:
            complexity_score += 1
        elif step_count <= 4:
            complexity_score += 2
        else:
            complexity_score += 3
        
        # 根据配料数量
        ingredient_count = len(ingredients)
        if ingredient_count <= 3:
            complexity_score += 1
        elif ingredient_count <= 6:
            complexity_score += 2
        else:
            complexity_score += 3
        
        # 根据关键词判断
        text = ' '.join(instructions).lower()
        complex_keywords = ['腌制', '调味酱', '炒糖色', '焯水', '过油']
        for keyword in complex_keywords:
            if keyword in text:
                complexity_score += 1
        
        if complexity_score <= 3:
            return "简单"
        elif complexity_score <= 5:
            return "中等"
        else:
            return "困难"
    
    def _estimate_servings(self, content: str, ingredients: List[str]) -> int:
        """估算份数"""
        # 查找明确的份数信息
        serving_patterns = [
            r'(\d+)\s*份',
            r'(\d+)\s*人份',
            r'(\d+)\s*人'
        ]
        
        for pattern in serving_patterns:
            match = re.search(pattern, content)
            if match:
                return int(match.group(1))
        
        # 根据配料重量估算
        total_weight = 0
        for ingredient in ingredients:
            weight_match = re.search(r'(\d+)g', ingredient)
            if weight_match:
                total_weight += int(weight_match.group(1))
        
        if total_weight > 1000:
            return 6
        elif total_weight > 500:
            return 4
        elif total_weight > 200:
            return 2
        else:
            return 1

class DataImporter:
    """数据导入器主类"""
    
    def __init__(self, project_path: str = r"e:\UGit\CookLikeHOC"):
        self.project_path = project_path
        self.parser = CookLikeHOCParser(project_path)
        self.recipes = []
        self.import_stats = {
            'total_files': 0,
            'successful': 0,
            'failed': 0,
            'categories': {}
        }
    
    def import_all_recipes(self) -> Dict:
        """导入所有菜谱数据"""
        logger.info("开始导入 CookLikeHOC 菜谱数据...")
        logger.info(f"项目路径: {self.project_path}")
        
        # 检查项目路径
        if not os.path.exists(self.project_path):
            raise FileNotFoundError(f"项目路径不存在: {self.project_path}")
        
        # 发现所有菜谱文件
        recipe_files = self.parser.discover_recipe_files()
        self.import_stats['total_files'] = len(recipe_files)
        
        # 解析每个文件
        for file_path in recipe_files:
            recipe = self.parser.parse_markdown_recipe(file_path)
            
            if recipe:
                self.recipes.append(recipe)
                self.import_stats['successful'] += 1
                
                # 统计分类
                category = recipe.category
                if category not in self.import_stats['categories']:
                    self.import_stats['categories'][category] = 0
                self.import_stats['categories'][category] += 1
                
            else:
                self.import_stats['failed'] += 1
        
        logger.info(f"导入完成! 成功: {self.import_stats['successful']}, 失败: {self.import_stats['failed']}")
        return self.import_stats
    
    def export_to_json(self, output_file: str = "cooklikehoc_recipes.json") -> str:
        """导出为 JSON 格式"""
        recipes_data = {
            'metadata': {
                'source': 'CookLikeHOC',
                'import_time': datetime.now().isoformat(),
                'total_recipes': len(self.recipes),
                'categories': list(self.import_stats['categories'].keys())
            },
            'recipes': [asdict(recipe) for recipe in self.recipes]
        }
        
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(recipes_data, f, ensure_ascii=False, indent=2)
        
        logger.info(f"数据已导出到: {output_file}")
        return output_file
    
    def export_to_android_assets(self, output_dir: str = "android_assets") -> str:
        """导出为 Android Assets 格式"""
        os.makedirs(output_dir, exist_ok=True)
        
        # 按分类导出
        for category, recipes in self._group_by_category().items():
            category_file = os.path.join(output_dir, f"{category}_recipes.json")
            category_data = {
                'category': category,
                'count': len(recipes),
                'recipes': [asdict(recipe) for recipe in recipes]
            }
            
            with open(category_file, 'w', encoding='utf-8') as f:
                json.dump(category_data, f, ensure_ascii=False, indent=2)
        
        # 创建索引文件
        index_file = os.path.join(output_dir, "recipes_index.json")
        index_data = {
            'total_recipes': len(self.recipes),
            'categories': {cat: len(recipes) for cat, recipes in self._group_by_category().items()},
            'files': [f"{cat}_recipes.json" for cat in self._group_by_category().keys()]
        }
        
        with open(index_file, 'w', encoding='utf-8') as f:
            json.dump(index_data, f, ensure_ascii=False, indent=2)
        
        logger.info(f"Android Assets 已导出到: {output_dir}")
        return output_dir
    
    def _group_by_category(self) -> Dict[str, List[Recipe]]:
        """按分类分组菜谱"""
        groups = {}
        for recipe in self.recipes:
            category = recipe.category
            if category not in groups:
                groups[category] = []
            groups[category].append(recipe)
        return groups
    
    def print_import_summary(self):
        """打印导入摘要"""
        print("\n" + "="*60)
        print("🍳 CookLikeHOC 菜谱导入摘要")
        print("="*60)
        print(f"📁 项目路径: {self.project_path}")
        print(f"📊 总文件数: {self.import_stats['total_files']}")
        print(f"✅ 成功导入: {self.import_stats['successful']}")
        print(f"❌ 导入失败: {self.import_stats['failed']}")
        print(f"📈 成功率: {self.import_stats['successful']/self.import_stats['total_files']*100:.1f}%")
        
        print("\n📋 分类统计:")
        for category, count in self.import_stats['categories'].items():
            print(f"  {category}: {count} 个菜谱")
        
        print("\n🔥 热门菜谱预览:")
        for i, recipe in enumerate(self.recipes[:5]):
            print(f"  {i+1}. {recipe.title} ({recipe.category})")
        
        print("="*60)

def main():
    """主函数"""
    try:
        # 创建导入器
        importer = DataImporter()
        
        # 导入所有菜谱
        stats = importer.import_all_recipes()
        
        # 打印摘要
        importer.print_import_summary()
        
        # 导出数据
        json_file = importer.export_to_json()
        android_dir = importer.export_to_android_assets()
        
        print(f"\n🎉 导入完成!")
        print(f"📄 JSON 文件: {json_file}")
        print(f"📱 Android Assets: {android_dir}")
        
        return True
        
    except Exception as e:
        logger.error(f"导入过程中发生错误: {e}")
        print(f"❌ 导入失败: {e}")
        return False

if __name__ == "__main__":
    main()