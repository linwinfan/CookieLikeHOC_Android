#!/usr/bin/env python3
"""
CookLikeHOC App Icon Generator
生成Android应用所需的各种尺寸图标
"""

from PIL import Image, ImageDraw, ImageFont
import os
import math

def create_icon(size):
    """创建指定尺寸的应用图标"""
    # 创建画布
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 背景渐变色（橙红到黄色）
    # 创建圆角矩形背景
    radius = int(size * 0.2)
    
    # 绘制渐变背景
    for y in range(size):
        # 计算渐变颜色
        ratio = y / size
        r = int(255 * (1 - ratio) + 255 * ratio)  # 255 to 255
        g = int(107 * (1 - ratio) + 210 * ratio)  # 107 to 210  
        b = int(53 * (1 - ratio) + 63 * ratio)   # 53 to 63
        
        color = (r, g, b, 255)
        draw.line([(0, y), (size, y)], fill=color)
    
    # 创建圆角蒙版
    mask = Image.new('L', (size, size), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle([0, 0, size, size], radius=radius, fill=255)
    
    # 应用圆角蒙版
    img.putalpha(mask)
    
    # 重新获取draw对象
    draw = ImageDraw.Draw(img)
    
    # 绘制厨师帽
    hat_center_x = size // 2
    hat_center_y = int(size * 0.35)
    hat_width = int(size * 0.4)
    hat_height = int(size * 0.25)
    
    # 帽子主体
    draw.ellipse([
        hat_center_x - hat_width//2, 
        hat_center_y - hat_height//2,
        hat_center_x + hat_width//2, 
        hat_center_y + hat_height//2
    ], fill=(255, 255, 255, 255))
    
    # 帽子顶部
    top_width = int(hat_width * 0.6)
    top_height = int(hat_height * 0.4)
    draw.ellipse([
        hat_center_x - top_width//2, 
        hat_center_y - hat_height//2 - top_height//3,
        hat_center_x + top_width//2, 
        hat_center_y - hat_height//2 + top_height//3
    ], fill=(255, 255, 255, 255))
    
    # 绘制餐具 - 叉子
    fork_x = int(size * 0.3)
    fork_y = int(size * 0.65)
    fork_length = int(size * 0.2)
    line_width = max(2, int(size * 0.02))
    
    # 叉子柄
    draw.line([
        (fork_x, fork_y), 
        (fork_x, fork_y + fork_length)
    ], fill=(255, 255, 255, 255), width=line_width)
    
    # 叉子齿
    for i in range(3):
        x_offset = int(size * 0.02 * (i - 1))
        draw.line([
            (fork_x + x_offset, fork_y - int(size * 0.03)), 
            (fork_x + x_offset, fork_y + int(size * 0.03))
        ], fill=(255, 255, 255, 255), width=line_width)
    
    # 绘制餐具 - 勺子
    spoon_x = int(size * 0.7)
    spoon_y = int(size * 0.65)
    
    # 勺子柄
    draw.line([
        (spoon_x, spoon_y), 
        (spoon_x, spoon_y + fork_length)
    ], fill=(255, 255, 255, 255), width=line_width)
    
    # 勺子头
    spoon_head_w = int(size * 0.05)
    spoon_head_h = int(size * 0.08)
    draw.ellipse([
        spoon_x - spoon_head_w//2, 
        spoon_y - int(size * 0.02) - spoon_head_h//2,
        spoon_x + spoon_head_w//2, 
        spoon_y - int(size * 0.02) + spoon_head_h//2
    ], fill=(255, 255, 255, 255))
    
    # 添加装饰性的星星
    star_positions = [
        (int(size * 0.15), int(size * 0.15), int(size * 0.03)),
        (int(size * 0.85), int(size * 0.2), int(size * 0.025)),
        (int(size * 0.2), int(size * 0.85), int(size * 0.02)),
        (int(size * 0.8), int(size * 0.85), int(size * 0.035))
    ]
    
    for star_x, star_y, star_size in star_positions:
        draw_star(draw, star_x, star_y, star_size, (255, 255, 255, 180))
    
    return img

def draw_star(draw, x, y, size, color):
    """绘制五角星"""
    points = []
    for i in range(10):
        angle = math.pi * i / 5
        if i % 2 == 0:
            # 外点
            px = x + size * math.cos(angle - math.pi/2)
            py = y + size * math.sin(angle - math.pi/2)
        else:
            # 内点
            px = x + size * 0.4 * math.cos(angle - math.pi/2)
            py = y + size * 0.4 * math.sin(angle - math.pi/2)
        points.append((px, py))
    
    draw.polygon(points, fill=color)

def create_all_icons():
    """创建所有尺寸的图标"""
    # Android图标尺寸配置
    icon_sizes = {
        'mipmap-mdpi': 48,
        'mipmap-hdpi': 72,
        'mipmap-xhdpi': 96,
        'mipmap-xxhdpi': 144,
        'mipmap-xxxhdpi': 192
    }
    
    # 创建输出目录
    base_path = 'app/src/main/res'
    
    for folder, size in icon_sizes.items():
        # 创建目录
        folder_path = os.path.join(base_path, folder)
        os.makedirs(folder_path, exist_ok=True)
        
        # 生成图标
        icon = create_icon(size)
        
        # 保存图标
        icon_path = os.path.join(folder_path, 'ic_launcher.png')
        icon.save(icon_path, 'PNG')
        
        # 同时保存为圆形图标（适配版本）
        icon_round_path = os.path.join(folder_path, 'ic_launcher_round.png')
        icon.save(icon_round_path, 'PNG')
        
        print(f"Generated: {icon_path} ({size}x{size})")
        print(f"Generated: {icon_round_path} ({size}x{size})")
    
    # 额外生成一些常用尺寸到当前目录
    extra_sizes = [48, 72, 96, 144, 192, 512]
    for size in extra_sizes:
        icon = create_icon(size)
        icon.save(f'ic_launcher_{size}x{size}.png', 'PNG')
        print(f"Generated: ic_launcher_{size}x{size}.png")

if __name__ == '__main__':
    print("CookLikeHOC App Icon Generator")
    print("=" * 40)
    
    try:
        create_all_icons()
        print("\n✅ 所有图标生成完成！")
        print("\n使用说明：")
        print("1. 图标已自动放置到正确的Android资源目录")
        print("2. 重新构建应用即可看到新图标")
        print("3. 额外的图标文件已保存到当前目录供备用")
        
    except Exception as e:
        print(f"❌ 生成图标时出错: {e}")
        print("请确保已安装 Pillow 库: pip install Pillow")