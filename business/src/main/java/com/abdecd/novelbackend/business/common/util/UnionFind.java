package com.abdecd.novelbackend.business.common.util;

public class UnionFind {
    private final int[] parent;  // 记录每个元素的父节点

    private int count;     // 记录连通分量的个数

    public UnionFind(int n) {
        parent = new int[n];
        count = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;  // 初始化时，每个元素都是它自己的父节点
        }
    }

    // 查找函数，查找元素p所在的集合的代表元
    public int find(int p) {
        if (parent[p] != p) {
            parent[p] = find(parent[p]);  // 路径压缩
        }
        return parent[p];
    }

    // 合并函数，将元素p和元素q所在的集合合并
    public void union(int p, int q) {
        int rootP = find(p);
        int rootQ = find(q);
        if (rootP == rootQ) {
            return;
        }
        parent[rootP] = rootQ;  // 将rootP所在的集合挂接到rootQ所在集合下
        count--;  // 合并后连通分量的数量减一
    }

    public int getCount() {
        return count;
    }
}
