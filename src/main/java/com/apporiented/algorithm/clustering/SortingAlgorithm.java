package com.apporiented.algorithm.clustering;

public interface SortingAlgorithm {
    public int[][] Linkage(double[] denseDistanceList, int dataNum);
    public int[] Sorting(int[][] linkageMatrix, int dataNum);
    public int[] Sorting(double[] denseDistanceList, int dataNum);
}
