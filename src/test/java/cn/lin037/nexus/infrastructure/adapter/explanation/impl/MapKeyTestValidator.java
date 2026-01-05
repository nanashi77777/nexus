package cn.lin037.nexus.infrastructure.adapter.explanation.impl;

import cn.lin037.nexus.infrastructure.adapter.explanation.dto.ChapterDto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证Map Key是否真的会失效
 */
public class MapKeyTestValidator {

    public static void main(String[] args) {
        System.out.println("=== Map Key 失效问题验证 ===");

        // 创建测试对象
        ChapterDto chapter = ChapterDto.builder()
                .sectionId(100L)
                .sectionTitle("测试章节")
                .build();

        List<String> testData = Arrays.asList("数据1", "数据2");

        // 创建Map并存储
        Map<ChapterDto, List<String>> map = new HashMap<>();
        map.put(chapter, testData);

        System.out.println("1. 存储到Map后，立即查找:");
        List<String> result1 = map.get(chapter);
        System.out.println("   结果: " + (result1 != null ? result1.size() + "个元素" : "null"));

        // 修改章节ID
        System.out.println("\n2. 修改章节ID: 100 -> " + (chapter.getSectionId() + 1000));
        chapter.setSectionId(chapter.getSectionId() + 1000);

        // 再次查找
        System.out.println("\n3. 修改ID后查找:");
        List<String> result2 = map.get(chapter);
        System.out.println("   结果: " + (result2 != null ? result2.size() + "个元素" : "null"));

        // 验证对象引用
        System.out.println("\n4. 验证对象引用:");
        for (ChapterDto key : map.keySet()) {
            System.out.println("   Map中的key == chapter对象: " + (key == chapter));
            System.out.println("   Map中的key.equals(chapter): " + key.equals(chapter));
            System.out.println("   Map中的key.hashCode(): " + key.hashCode());
            System.out.println("   chapter.hashCode(): " + chapter.hashCode());
        }

        System.out.println("\n=== 结论 ===");
        if (result2 != null) {
            System.out.println("✅ Map Key 没有失效，您的建议是正确的！");
            System.out.println("   可以直接修改原Map中的对象，无需创建新Map");
        } else {
            System.out.println("❌ Map Key 确实失效了，需要特殊处理");
        }
    }
}
