package com.apporiented.algorithm.clustering;
/*
    Perform hierarchy clustering.
    Parameters
    ----------
    dists : ndarray
        A condensed matrix stores the pairwise distances of the observations.
    n : int
        The number of observations.
    method : int
        The linkage method. 0: single 1: complete 2: average 3: centroid
        4: median 5: ward 6: weighted
    Returns
    -------
    Z : ndarray, shape (n - 1, 4)
        Computed linkage matrix.
*/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ClusterSorting implements SortingAlgorithm {
    @Override
    public int[][] Linkage(double[] denseDistances, int n) {
        /* Argument check */
        if (denseDistances == null || denseDistances.length == 0){
            throw new IllegalArgumentException("Invalid dense distance list");
        }
        if (denseDistances.length != n * (n - 1) / 2){
            throw new IllegalArgumentException("Invalid data num");
        }

        /* Z arrays, At the
            :math:`i`-th iteration, clusters with indices ``Z_clus_matrix[i][0]`` and
            `Z_clus_matrix[i][1]`` are combined to form cluster :math:`n + i`. A
            cluster with an index less than :math:`n` corresponds to one of
            the :math:`n` original observations. The distance between
            clusters ``Z_clus_matrix[i][0]`` and
            `Z_clus_matrix[i][1]`` is given by ``Z_array_distance_xy[i]``. The
            value ``Z_clus_matrix[i][2]`` represents the number of original
            observations in the newly formed cluster.
        */
//        int[] Z_array_clus_x = new int[n-1];
//        int[] Z_array_clus_y = new int[n-1];
        double[] Z_array_distance_xy = new double[n-1];
//        int[] Z_array_clus_n = new int[n-1];
        int[][] Z_clus_matrix = new int[n-1][3];

//        int i, j, k, x, y, nx, ny, ni, id_x, id_y, id_i;
//        int i_start = 0;
//        double current_min_dis;
        // inter-cluster dists: denseDistances
        double[] D = denseDistances;
        // map the indices to node ids
        int[] id_map = new int[n];
        // init id_map
        for (int i=0; i<n; i++) { id_map[i] = i; }

        for (int k=0; k<n-1; k++) {
            int x = 0, y = 0, id_x, id_y, nx, ny;
            //find two closest clusters x, y (x < y)
            double current_min_dis = Double.POSITIVE_INFINITY;

            for (int i =0; i<n-1; i++) {
                if (id_map[i] == -1) {
                    continue;
                }

                // condensed index start from (i, i+1)
                int i_start = getCondensedIndex(n, i, i+1);
                for (int j=0; j<n-i-1; j++) {
                    if (D[i_start + j] < current_min_dis) {
                        current_min_dis = D[i_start + j];
                        x = i;
                        y = i + j + 1;
                    }
                }
            }

            id_x = id_map[x];
            id_y = id_map[y];

            // get the original numbers of points in clusters x and y
            nx = id_x < n ? 1 : Z_clus_matrix[id_x - n][2];
            ny = id_y < n ? 1 : Z_clus_matrix[id_y - n][2];

            // record the new code
            Z_clus_matrix[k][0] = min(id_x, id_y);
            Z_clus_matrix[k][1] = max(id_x, id_y);
            Z_clus_matrix[k][2] = nx + ny;
            Z_array_distance_xy[k] = current_min_dis;

            id_map[x] = -1; // cluster x will be dropped
            id_map[y] = n + k; // cluster y will be replaced with the new cluster

            // update the distance matrix
            for (int i=0; i<n; i++){
                int id_i = id_map[i];
                if (id_i == -1 || id_i == n + k){
                    continue;
                }

                int ni = id_i < n ? 1 : Z_clus_matrix[id_i - n][2];
                D[getCondensedIndex(n, i, y)] = calculateAvgDistance(D[getCondensedIndex(n, i, x)],
                                                            D[getCondensedIndex(n, i, y)],
                                                            nx, ny);
                if (i < x) {
                    D[getCondensedIndex(n, i, x)] = Double.POSITIVE_INFINITY;
                }
            }
        }
//        System.out.println(Arrays.toString(Z_array_distance_xy));
        return Z_clus_matrix;
    }

    @Override
    public int[] Sorting(double[] denseDistances, int n) {
        int[][] Z_tree = Linkage(denseDistances, n);
        return Sorting(Z_tree, n);
    }

    @Override
    public int[] Sorting(int[][] Z_tree, int n) {
        HashMap<Integer, Integer> leftLeaf = new HashMap<>();
        HashMap<Integer, Integer> rightLeaf = new HashMap<>();
        for (int i = 0; i< n-1; i++) {
            leftLeaf.put(i + n, Z_tree[i][0]);
            rightLeaf.put(i + n, Z_tree[i][1]);
        }
        Stack<Integer> stack = new Stack<>();
        int node = 2 * n - 2;
        int[] sortedList = new int[n];
        int leaf_id = 0;
        while (node != -1 || !stack.empty()){
            if (node!=-1){
                stack.push(node);
                node = leftLeaf.getOrDefault(node, -1);
            } else {
                int leaf = stack.pop();
                if (leaf>=0 && leaf<n){
                    sortedList[leaf_id] = leaf;
                    leaf_id += 1;
                }
                node = rightLeaf.getOrDefault(leaf, -1);
            }
        }

        return sortedList;
    }

    private int getCondensedIndex(int n, int i, int j) {
        /* Calculate the condensed index of element (i, j) in an n*n condensed matrix */
        if (i < j) {
            return n * i - (i * (i + 1) / 2) + (j - i - 1);
        }
        else if (i > j) {
            return n * j - (j * (j + 1) / 2) + (i - j - 1);
        }
        else{
            return 0;
        }
    }

    private double calculateAvgDistance(double d_xi, double d_yi, int size_x, int size_y) {
        return (size_x * d_xi + size_y * d_yi) / (size_x + size_y);
    }

    public static void main(String[] args){
        ClusterSorting clusterSorting = new ClusterSorting();
        int[][] Z = {{0,1,12},{3,4,13},{6,7,14},{9,10,15},{2,12,16},{5,13,17},{8,14,18},{11,15,19},{16,17,20},{18,19,21},{20,21,22}};
        int[] res = clusterSorting.Sorting(Z, 12);
        System.out.println(Arrays.toString(res));
        double[] dis = {1.        , 1.        , 4.        , 3.        , 4.12310563,
                4.        , 3.        , 4.12310563, 5.65685425, 5.        ,
                5.        , 1.41421356, 3.        , 2.        , 3.16227766,
                4.12310563, 3.16227766, 4.        , 5.        , 4.24264069,
                4.47213595, 4.12310563, 3.16227766, 4.        , 3.        ,
                2.        , 3.16227766, 5.        , 4.47213595, 4.24264069,
                1.        , 1.        , 5.65685425, 5.        , 5.        ,
                4.        , 3.        , 4.12310563, 1.41421356, 5.        ,
                4.24264069, 4.47213595, 4.12310563, 3.16227766, 4.        ,
                5.        , 4.47213595, 4.24264069, 3.        , 2.        ,
                3.16227766, 1.        , 1.        , 4.        , 4.12310563,
                3.        , 1.41421356, 4.12310563, 4.        , 3.16227766,
                3.        , 3.16227766, 2.        , 1.        , 1.        ,
                1.41421356};
        int[][] Z_arr = clusterSorting.Linkage(dis, 12);
        System.out.println(Arrays.deepToString(Z_arr));
        int[] res1 = clusterSorting.Sorting(Z_arr, 12);
        System.out.println(Arrays.toString(res1));
    }
}
