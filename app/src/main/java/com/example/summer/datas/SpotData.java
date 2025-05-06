
package com.example.summer.datas;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// 定义树节点类
class TreeNode {
    String name;
    int searchTimes;
    int currentCrowd;
    int lastCrowd;
    List<TreeNode> children;
    boolean isToilet; // **新增：是否为公共厕所标签**

    // 带标签的构造方法
    public TreeNode(String name, boolean isToilet) {
        this.name = name;
        this.isToilet = isToilet; // 初始化标签
        this.searchTimes = 0;
        this.currentCrowd = 0;
        this.lastCrowd = 0;
        this.children = new ArrayList<>();
    }

    // 无标签的构造方法（普通节点默认isToilet=false）
    public TreeNode(String name) {
        this(name, false); // 调用带标签的构造方法
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }
}

class CrowdSimulator {
    private static final int MAX_CHANGE = 200;       // 单次最大人流量变化
    private static final double ALPHA = 0.7;         // 平滑系数
    private static final Map<String, Integer> TIME_PERIOD_WEIGHT = new HashMap<>();
    // **公共厕所人流量限制**
    private static final int TOILET_MIN_CROWD = 5;
    private static final int TOILET_MAX_CROWD = 25;


    static {
        // 预设时间段基础人流量（高峰时段权重高，低谷权重低）
        TIME_PERIOD_WEIGHT.put("09:00-11:00", 180);
        TIME_PERIOD_WEIGHT.put("14:00-16:00", 150);
        TIME_PERIOD_WEIGHT.put("19:00-21:00", 60);
        // 其他时间默认权重80
    }

    // 每5分钟更新一次人流量
    public void updateCrowd(TreeNode node) {
        String currentTime = getCurrentTime(); // 获取当前时间（格式：HH:mm）
        // 根据当前时间段获取基础人流量
        int baseCrowd = TIME_PERIOD_WEIGHT.getOrDefault(getTimePeriod(currentTime), 80);

        // 生成合理波动的随机数
        int randomDelta = new Random().nextInt(2 * MAX_CHANGE + 1) - MAX_CHANGE;
        int newCrowd = baseCrowd + randomDelta;

        // 指数平滑处理，避免突变
        node.currentCrowd = (int) (ALPHA * newCrowd + (1 - ALPHA) * node.lastCrowd);
        // 更新当前人流量
        node.lastCrowd = node.currentCrowd;

        // 限制人流量在合理范围（如0-2000）
        // **通过标签判断是否为公共厕所**
        if (node.isToilet) { // 灵活判断，不依赖名称
            node.currentCrowd = Math.max(TOILET_MIN_CROWD, Math.min(TOILET_MAX_CROWD, node.currentCrowd));
        } else {
            // 其他节点保持原范围
            node.currentCrowd = Math.max(2, Math.min(2000, node.currentCrowd));
        }
    }

    private String getCurrentTime() {
        // 获取当前时间（格式：HH:mm）
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private String getTimePeriod(String time) {
        // 将时间映射到预设时间段（如"10:30"映射到"09:00-11:00"）
        // 具体实现根据需求调整
        // 解析小时部分（HH）进行判断
        int hour = Integer.parseInt(time.split(":")[0]);

        if (hour >= 9 && hour <= 11) {
            return "09:00-11:00";
        } else if (hour >= 14 && hour <= 16) {
            return "14:00-16:00";
        } else if (hour >= 19 && hour <= 21) {
            return "19:00-21:00";
        } else {
            return "other";
        }
    }
}

public class SpotData {
    private TreeNode root;
    private CrowdSimulator crowdSimulator;
    private ScheduledExecutorService scheduler;

    public SpotData() {
        crowdSimulator = new CrowdSimulator();
        // 初始化根节点为承德避暑山庄
        root = new TreeNode("承德避暑山庄");

        // 自然风光类别
        TreeNode naturalScenery = new TreeNode("自然风光");
        // 湖泊区
        TreeNode lakeArea = new TreeNode("湖泊区");
        lakeArea.addChild(new TreeNode("如意湖"));
        lakeArea.addChild(new TreeNode("上湖"));
        lakeArea.addChild(new TreeNode("热河泉"));
        lakeArea.addChild(new TreeNode("内湖"));
        // 山峦区
        TreeNode mountainArea = new TreeNode("山峦区");
        mountainArea.addChild(new TreeNode("南山积雪"));
        mountainArea.addChild(new TreeNode("四面云山"));
        // 平原区
        TreeNode plainArea = new TreeNode("平原区");
        plainArea.addChild(new TreeNode("万树园"));
        plainArea.addChild(new TreeNode("甫田丛樾"));

        naturalScenery.addChild(lakeArea);
        naturalScenery.addChild(mountainArea);
        naturalScenery.addChild(plainArea);

        // 历史文化类别
        TreeNode historicalCulture = new TreeNode("历史文化");
        // 寺庙建筑
        TreeNode templeBuildings = new TreeNode("寺庙建筑");
        templeBuildings.addChild(new TreeNode("溥仁寺"));
        templeBuildings.addChild(new TreeNode("普宁寺"));
        templeBuildings.addChild(new TreeNode("法林寺"));
        templeBuildings.addChild(new TreeNode("碧峰寺遗址"));
        templeBuildings.addChild(new TreeNode("灵泽龙王庙"));
        templeBuildings.addChild(new TreeNode("永佑寺"));


        // 园林景观
        TreeNode gardenLandscape = new TreeNode("园林景观");
        gardenLandscape.addChild(new TreeNode("曲水荷香"));
        gardenLandscape.addChild(new TreeNode("濠濮间想"));
        // 碑刻文化
        TreeNode steleCulture = new TreeNode("碑刻文化");
        steleCulture.addChild(new TreeNode("避暑山庄碑"));
        steleCulture.addChild(new TreeNode("双湖夹镜碑"));
        steleCulture.addChild(new TreeNode("绿毯八韵碑"));

        // 宫殿建筑
        TreeNode palaceBuildings = new TreeNode("宫殿建筑");
        palaceBuildings.addChild(new TreeNode("澹泊敬诚殿"));
        palaceBuildings.addChild(new TreeNode("四知书屋"));
        palaceBuildings.addChild(new TreeNode("烟波致爽殿"));

        historicalCulture.addChild(templeBuildings);
        historicalCulture.addChild(gardenLandscape);
        historicalCulture.addChild(steleCulture);
        historicalCulture.addChild(palaceBuildings);

        // 景区设施类别
        TreeNode scenicFacilities = new TreeNode("景区设施");

        // 在景区厕所下添加公共厕所1、2、3、4四个子节点
        TreeNode publicToilets = new TreeNode("景区厕所"); // 父节点无需标记
        // 子节点均标记为公共厕所
        publicToilets.addChild(new TreeNode("公共厕所1", true));
        publicToilets.addChild(new TreeNode("公共厕所2", true));
        publicToilets.addChild(new TreeNode("公共厕所3", true));
        publicToilets.addChild(new TreeNode("公共厕所4", true));
        scenicFacilities.addChild(publicToilets);

        // 在停车场下添加地下停车场、地上停车场两个子节点
        TreeNode parkingLot = new TreeNode("停车场");
        parkingLot.addChild(new TreeNode("地下停车场"));
        parkingLot.addChild(new TreeNode("地上停车场"));
        scenicFacilities.addChild(parkingLot);

        // 在景区出入口添加丽正门、德汇门两个子节点
        TreeNode entranceExit = new TreeNode("景区出入口");
        entranceExit.addChild(new TreeNode("丽正门"));
        entranceExit.addChild(new TreeNode("德汇门"));
        scenicFacilities.addChild(entranceExit);

        scenicFacilities.addChild(new TreeNode("游客中心"));

        // 将类别节点添加到根节点
        root.addChild(naturalScenery);
        root.addChild(historicalCulture);
        root.addChild(scenicFacilities);

        // 初始化人流量
        initializeCrowd(root);

        // 初始化调度器
        scheduler = Executors.newScheduledThreadPool(1);
        // 每5分钟更新一次人流量
        scheduler.scheduleAtFixedRate(() -> updateAllCrowds(root), 0, 5, TimeUnit.MINUTES);
    }

    private void initializeCrowd(TreeNode node) {
        // 首次初始化时递归地设置初始人流量（可调用一次更新）
        crowdSimulator.updateCrowd(node);
        for (TreeNode child : node.children) {
            initializeCrowd(child);
        }
    }

    private void updateAllCrowds(TreeNode node) {
        crowdSimulator.updateCrowd(node);
        for (TreeNode child : node.children) {
            updateAllCrowds(child);
        }
    }

    // 深度优先搜索
    public List<String> dfsSearch(String category) {
        List<String> result = new ArrayList<>();
        dfs(root, category, result);
        return result;
    }

    private void dfs(TreeNode node, String category, List<String> result) {
        if (node.name.equals(category)) {
            collectChildrenNames(node, result);
            return;
        }
        for (TreeNode child : node.children) {
            dfs(child, category, result);
        }
    }

    private void collectChildrenNames(TreeNode node, List<String> result) {
        for (TreeNode child : node.children) {
            if (child.children.isEmpty()) {
                result.add(child.name + "（人流量：" + child.currentCrowd + "）");
            } else {
                collectChildrenNames(child, result);
            }
        }
    }

    // 广度优先搜索
    public List<String> bfsSearch(String category) {
        List<String> result = new ArrayList<>();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            TreeNode current = queue.poll();
            if (current.name.equals(category)) {
                collectChildrenNames(current, result);
                break;
            }
            queue.addAll(current.children);
        }
        return result;
    }

    // 根据输入的景点类别选择合适的搜索算法
    public List<String> optimizedSearch(String category) {
        // 简单判断：如果输入的类别是根节点的直接子节点，使用BFS
        for (TreeNode child : root.children) {
            if (child.name.equals(category)) {
                return bfsSearch(category);
            }
        }
        // 否则使用DFS
        return dfsSearch(category);
    }

    // 输入一个地点的名称，返回该地点的人流量
    public int getCrowdByLocationName(String locationName) {
        TreeNode node = findNode(root, locationName);
        return node != null ? node.currentCrowd : -1;
    }
//优化搜索
    private TreeNode findNode(TreeNode node, String name) {
        if (node.name.equals(name)) {
            return node;
        }
        for (TreeNode child : node.children) {
            TreeNode found = findNode(child, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }



    // 输入一个地点的名称，将该地点对应的searchtimes加1，若为类别，则将该分类下所有地点的searchtimes加1
    public void increaseSearchTimes(String locationName) {
        TreeNode node = findNode(root, locationName);
        if (node != null) {
            if (node.children.isEmpty()) {
                node.searchTimes++;
            } else {
                increaseSearchTimesRecursive(node);
            }
        }
    }

    private void increaseSearchTimesRecursive(TreeNode node) {
        if (node.children.isEmpty()) {
            node.searchTimes++;
        } else {
            for (TreeNode child : node.children) {
                increaseSearchTimesRecursive(child);
            }
        }
    }


    // 新添加的方法，接受节点名，将指定节点下的所有叶节点的name属性存入字符串数组并返回
    public List<String> getAllLeafNodeNames(String nodeName) {
        List<String> result = new ArrayList<>();
        TreeNode targetNode = findNode(root, nodeName);
        if (targetNode != null) {
            getAllLeafNodeNamesRecursive(targetNode, result);
        }
        return result;
    }

    private void getAllLeafNodeNamesRecursive(TreeNode node, List<String> result) {
        if (node.children.isEmpty()) {
            result.add("承德避暑山庄"+node.name);
        } else {
            for (TreeNode child : node.children) {
                getAllLeafNodeNamesRecursive(child, result);
            }
        }
    }
}