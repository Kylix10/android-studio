package com.example.summer.utils;

import android.util.Log;
import com.example.summer.datas.SpotData;

import java.util.*;

/**
 * 景点推荐器，用于根据用户年龄、性别以及搜索历史推荐景点
 */
public class SpotRecommender {
    private SpotData spotData;
    private String gender;
    private int age;

    // 景点简介信息
    private static final Map<String, String> spotDescriptions = new HashMap<>();
    static {
        // 自然风光类
        spotDescriptions.put("如意湖", "如意湖是避暑山庄的主要人工湖泊之一，水面开阔，湖畔风景优美，是游船观景和拍照的理想地点。");
        spotDescriptions.put("上湖", "上湖位于避暑山庄东北部，湖面开阔，环境清幽，是理想的赏景休憩之处。");
        spotDescriptions.put("热河泉", "热河泉是避暑山庄著名的温泉之一，四季不竭，水温恒定，具有一定的医疗保健功效。");
        spotDescriptions.put("内湖", "内湖是避暑山庄的核心湖区，环绕于宫殿建筑周围，湖水清澈，景色宜人。");
        spotDescriptions.put("南山积雪", "南山积雪是避暑山庄著名的'三十六景'之一，远眺雪山，景色壮观。");
        spotDescriptions.put("四面云山", "四面云山是避暑山庄的自然景观，山峦环绕，云雾缭绕，景色秀美。");
        spotDescriptions.put("万树园", "万树园是避暑山庄内的一处大型植物园，园内林木葱郁，花草繁茂，是亲近自然的好去处。");
        spotDescriptions.put("甫田丛樾", "甫田丛樾是一处模仿江南田园风光的区域，田园风光与宫廷园林相结合，景色优美。");

        // 历史文化类
        spotDescriptions.put("溥仁寺", "溥仁寺是避暑山庄外八庙之一，建筑风格独特，融合了汉、藏、蒙古等多种建筑风格。");
        spotDescriptions.put("普宁寺", "普宁寺是避暑山庄外八庙中规模最大的寺庙，内有世界上最大的木制佛像之一。");
        spotDescriptions.put("法林寺", "法林寺是避暑山庄外八庙之一，寺内供奉释迦牟尼佛，建筑风格独特。");
        spotDescriptions.put("碧峰寺遗址", "碧峰寺遗址保存了清代寺庙建筑的重要历史遗迹，具有重要的考古价值。");
        spotDescriptions.put("灵泽龙王庙", "灵泽龙王庙供奉龙王，是祈雨祭祀的场所，建筑形式独特，装饰精美。");
        spotDescriptions.put("永佑寺", "永佑寺是避暑山庄外八庙之一，寺内壁画和雕塑艺术精湛，是研究清代艺术的重要场所。");
        spotDescriptions.put("曲水荷香", "曲水荷香是避暑山庄的著名景点，水道蜿蜒，荷花盛开，景色秀美。");
        spotDescriptions.put("濠濮间想", "濠濮间想是避暑山庄的一处园林景观，取自《庄子·秋水》典故，环境优雅。");
        spotDescriptions.put("避暑山庄碑", "避暑山庄碑是清代皇家文化的重要遗存，碑文由康熙皇帝亲自撰写，具有重要的历史文化价值。");
        spotDescriptions.put("双湖夹镜碑", "双湖夹镜碑是避暑山庄的景观碑刻，镜湖倒影，风景如画。");
        spotDescriptions.put("绿毯八韵碑", "绿毯八韵碑是避暑山庄的文化景观，碑上刻有描写避暑山庄自然景色的诗词。");
        spotDescriptions.put("澹泊敬诚殿", "澹泊敬诚殿是避暑山庄的主要宫殿建筑之一，是清代皇帝处理政务的地方，建筑风格典雅庄重。");
        spotDescriptions.put("四知书屋", "四知书屋是避暑山庄的一处静谧书房，是清代皇帝读书学习的地方，环境幽静。");
        spotDescriptions.put("烟波致爽殿", "烟波致爽殿是避暑山庄的主要宫殿之一，位于湖畔，可以欣赏湖光山色，是休闲赏景的好去处。");
    }

    // 景点分类映射
    private static final Map<String, String> spotCategories = new HashMap<>();
    static {
        // 自然风光类
        spotCategories.put("如意湖", "自然风光");
        spotCategories.put("上湖", "自然风光");
        spotCategories.put("热河泉", "自然风光");
        spotCategories.put("内湖", "自然风光");
        spotCategories.put("南山积雪", "自然风光");
        spotCategories.put("四面云山", "自然风光");
        spotCategories.put("万树园", "自然风光");
        spotCategories.put("甫田丛樾", "自然风光");

        // 历史文化类
        spotCategories.put("溥仁寺", "历史文化");
        spotCategories.put("普宁寺", "历史文化");
        spotCategories.put("法林寺", "历史文化");
        spotCategories.put("碧峰寺遗址", "历史文化");
        spotCategories.put("灵泽龙王庙", "历史文化");
        spotCategories.put("永佑寺", "历史文化");
        spotCategories.put("曲水荷香", "历史文化");
        spotCategories.put("濠濮间想", "历史文化");
        spotCategories.put("避暑山庄碑", "历史文化");
        spotCategories.put("双湖夹镜碑", "历史文化");
        spotCategories.put("绿毯八韵碑", "历史文化");
        spotCategories.put("澹泊敬诚殿", "历史文化");
        spotCategories.put("四知书屋", "历史文化");
        spotCategories.put("烟波致爽殿", "历史文化");
    }

    public SpotRecommender(SpotData spotData, String gender, int age) {
        this.spotData = spotData;
        this.gender = gender;
        this.age = age;
    }

    /**
     * 推荐三个景点
     */
    public List<Map<String, String>> recommendSpots() {
        List<String> raw = getAllSpots();             // 可能带“（人流量：...）”
        List<String> names = stripCrowdSuffix(raw);   // 去掉后缀，得到纯名称

        // 计算权重并抽样
        Map<String, Double> scores = calculateSpotScores(names);
        List<String> picks = pickMultiple(scores, 3);

        List<Map<String, String>> result = new ArrayList<>();
        for (String name : picks) {
            Map<String, String> info = new HashMap<>();
            info.put("name", name);
            info.put("category", spotCategories.getOrDefault(name, "未分类"));
            info.put("description", spotDescriptions.getOrDefault(name, "这是一个美丽的景点，值得一游。"));
            result.add(info);
        }
        return result;
    }

    /**
     * 去掉“（人流量：...）”后缀
     */
    private List<String> stripCrowdSuffix(List<String> raw) {
        List<String> clean = new ArrayList<>(raw.size());
        for (String s : raw) {
            int idx = s.indexOf('（');
            if (idx > 0) clean.add(s.substring(0, idx));
            else          clean.add(s);
        }
        return clean;
    }

    /**
     * 从 weightMap 中按权重随机挑 num 个不重复的键
     */
    private List<String> pickMultiple(Map<String, Double> weightMap, int num) {
        Map<String, Double> map = new HashMap<>(weightMap);
        List<String> picks = new ArrayList<>(num);
        for (int i = 0; i < num && !map.isEmpty(); i++) {
            String pick = pickWeightedRandom(map);
            picks.add(pick);
            map.remove(pick);
        }
        return picks;
    }

    private String pickWeightedRandom(Map<String, Double> weightMap) {
        double total = 0;
        for (double w : weightMap.values()) total += w;
        double r = Math.random() * total;
        for (Map.Entry<String, Double> e : weightMap.entrySet()) {
            r -= e.getValue();
            if (r <= 0) return e.getKey();
        }
        // 兜底
        return weightMap.keySet().iterator().next();
    }

    private Map<String, Double> calculateSpotScores(List<String> spots) {
        Map<String, Double> scores = new HashMap<>();
        for (String name : spots) {
            double score = getSpotSearchTimes(name) * 20.0
                    + calculateAgeScore(name) * 1.5
                    + calculateGenderScore(name) * 1.5;
            score *= 0.95 + Math.random() * 0.1;
            scores.put(name, score);
        }
        return scores;
    }

    private double calculateAgeScore(String name) {
        String cat = spotCategories.getOrDefault(name, "");
        if (age < 18) {
            if (cat.equals("自然风光")) return 40;
            if (cat.equals("历史文化")) return 20;
        } else if (age < 40) {
            if (cat.equals("自然风光")) return 30;
            if (cat.equals("历史文化")) return 30;
        } else if (age < 60) {
            if (cat.equals("自然风光")) return 20;
            if (cat.equals("历史文化")) return 40;
        } else {
            if (cat.equals("自然风光")) return 15;
            if (cat.equals("历史文化")) return 35;
        }
        return 0;
    }

    private double calculateGenderScore(String name) {
        String cat = spotCategories.getOrDefault(name, "");
        if ("男".equals(gender)) {
            if (cat.equals("自然风光")) return 25;
            if (cat.equals("历史文化")) return 35;
        } else {
            if (cat.equals("自然风光")) return 35;
            if (cat.equals("历史文化")) return 25;
        }
        return 0;
    }

    private int getSpotSearchTimes(String name) {
        try {
            spotData.increaseSearchTimes(name);  // 先累加一次
            int crowd = spotData.getCrowdByLocationName(name);
            return Math.max(1, crowd % 50 + 1);
        } catch (Exception e) {
            Log.e("SpotRecommender", "Error getting search times", e);
            return 1;
        }
    }

    private List<String> getAllSpots() {
        List<String> all = new ArrayList<>();
        try {
            all.addAll(spotData.optimizedSearch("自然风光"));
            all.addAll(spotData.optimizedSearch("历史文化"));
        } catch (Exception e) {
            Log.e("SpotRecommender", "Error getting spots", e);
        }
        return all;
    }

    public String getMostSearchedCategory() {
        // 简单比较两大类总 searchTimes
        int f = 0, h = 0;
        for (String raw : getAllSpots()) {
            String name = raw.contains("（") ? raw.substring(0, raw.indexOf('（')) : raw;
            String cat = spotCategories.getOrDefault(name, "");
            int t = getSpotSearchTimes(name);
            if (cat.equals("自然风光")) f += t;
            if (cat.equals("历史文化")) h += t;
        }
        return f >= h ? "自然风光" : "历史文化";
    }

    public List<Map<String, String>> getSpotsByCategory(String category) {
        List<Map<String, String>> list = new ArrayList<>();
        for (String raw : getAllSpots()) {
            String name = raw.contains("（") ? raw.substring(0, raw.indexOf('（')) : raw;
            if (!spotCategories.getOrDefault(name, "").equals(category)) continue;
            Map<String, String> info = new HashMap<>();
            info.put("name", name);
            info.put("category", category);
            info.put("description", spotDescriptions.getOrDefault(name,
                    "这是一个美丽的景点，值得一游。"));
            list.add(info);
        }
        return list;
    }
}


