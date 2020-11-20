package com.apporiented.algorithm.clustering;


import java.util.*;
// img coef [ 5.46252338e-02,  6.89079784e-16,  7.32197040e-02,
//        -2.81635123e-02, -4.50561917e-02,  1.49390538e-01,
//         5.65949171e-02,  7.39389311e-01]
// img features: [shape_matrix,cattop_matrix,catsub_matrix,catdiff_matrix,catnull_matrix,shopname_matrix,rule_dis,cosinedist]
public class MixedPDist {
//    private static final String CVID = "cvid";
//    private static final String CATEGORY_TOP_ID = "categoryTopId";
//    private static final String CATEGORY_SUB_ID = "categorySubId";
//    private static final String REVIEW_BODY = "reviewBody";
//    private static final String EFFECT_RULE_ID = "effectRuleId";

    public double[] getTxtPDist(ArrayList<Integer> catTop, ArrayList<Integer> catSub, ArrayList<String> reviewbody, ArrayList<String> effectRuleId, int n){
//        ArrayList<Integer> cvid = new ArrayList<Integer>();
//        ArrayList<Integer> catTop = new ArrayList<Integer>();
//        ArrayList<Integer> catSub = new ArrayList<Integer>();
//        ArrayList<String> reviewbody = new ArrayList<String>();
//        ArrayList<String> effectRuleId = new ArrayList<String>();
//        for(Map<String, Object> map : mapList){
//            /* Argument checks */
//            if (map.get(CVID) == null) {
//                throw new IllegalArgumentException("cvid can't be null");
//            }
//            cvid.add((Integer)map.get(CVID));
//            catTop.add((Integer)map.get(CATEGORY_TOP_ID)); //可能为null
//            catSub.add((Integer)map.get(CATEGORY_SUB_ID)); //可能为null
//            reviewbody.add(map.get(REVIEW_BODY)==null ? "":map.get(REVIEW_BODY).toString()); //可能为null，需转换
//            effectRuleId.add(map.get(EFFECT_RULE_ID)==null ? "":map.get(EFFECT_RULE_ID).toString()); //可能为null，需转换
//        }

        double[] reviewbodyDis, effectRuleDis, categoryDis;
        reviewbodyDis = getLevenshteinDist(reviewbody, n);
        effectRuleDis = getEffectRuleDist(effectRuleId, n);
        categoryDis = getTxtCategoryDist(catTop, catSub, n);
        double[] pDist = new double[n];
        for (int i=0; i<n; i++){
            pDist[i] = Math.max(0.0, 0.865 * reviewbodyDis[i] + 0.135 * effectRuleDis[i] + 0.1 * categoryDis[i]);
        }
        return pDist;

    }

    public double[] getImgPDist(ArrayList<Integer> catTop, ArrayList<Integer> catSub, ArrayList<Integer> shopId, ArrayList<Float[]> imgFeature, ArrayList<String> effectRuleId, int n){
        double[] imgFeatureDis, effectRuleDis, categoryDis, shopidDis;
        imgFeatureDis = getCosineDist(imgFeature, n, 2048);
        effectRuleDis = getEffectRuleDist(effectRuleId, n);
        categoryDis = getTxtCategoryDist(catTop, catSub, n);
        shopidDis = getShopnameDist(shopId, n);
        double[] pDist = new double[n];
        for (int i=0; i<n; i++){
            pDist[i] = Math.max(0.0, 0.865 * imgFeatureDis[i] + 0.135 * effectRuleDis[i] + 0.1 * categoryDis[i] + 0.1 * shopidDis[i]);
        }
        return pDist;
    }
    // 文字编辑距离（截断长度300）
    public double[] getLevenshteinDist(ArrayList<String> X, int n){
        double[] res = new double[n * (n-1) / 2];
        int i,j,k=0;
        for (i=0; i<n-1; i++){
            for (j=i+1; j<n; j++){
                res[k] = 1 - LevenshteinRatio(X.get(i).substring(0,300), X.get(j).substring(0,300));
                k+=1;
            }
        }
        return res;
    }
    // 生效规则距离
    public double[] getEffectRuleDist(ArrayList<String> X, int n){
        double[] res = new double[n * (n-1) / 2];
        int i,j,k=0;
        for (i=0; i<n-1; i++){
            for (j=i+1; j<n; j++){
                Set<String> ruleSet1 = new HashSet<>(Arrays.asList(X.get(i).split(",")));
                Set<String> ruleSet2 = new HashSet<>(Arrays.asList(X.get(j).split(",")));
                res[k] = 1 - stringSetDist(ruleSet1, ruleSet2);
                k+=1;
            }
        }
        return res;
    }
    // 文字类目距离
    public double[] getTxtCategoryDist(ArrayList<Integer> CatTop, ArrayList<Integer> CatSub, int n){
        double[] res = new double[n * (n-1) / 2];
        int i,j,k=0;
        for (i=0; i<n-1; i++){
            for (j=i+1; j<n; j++){
                if (CatTop.get(i) != null && CatTop.get(i).equals(CatTop.get(j))){
                    if (CatSub.get(i) != null && CatSub.get(i).equals(CatSub.get(j))){
                        res[k] = 0.0;
                    }
                    else {
                        res[k] = 0.5;
                    }
                }
                else {
                    res[k] = 1;
                }
                k+=1;
            }
        }
        return res;
    }
    // 图像类目距离
    public double[] getImgCategoryDist(String[] CatTop, String[] CatSub, int n){
        double[] res = new double[n * (n-1) / 2];
        int i,j,k=0;
        for (i=0; i<n-1; i++){
            for (j=i+1; j<n; j++){
                if (CatTop[i]==null || CatTop[j] == null || CatSub[i] == null || CatSub[j] == null) {
                    res[k] = 0.045;
                }
                else{
                    if (CatTop[i].equals(CatTop[j]) && CatSub[i].equals(CatSub[j])){
                        res[k] = - 0.073;
                    }
                    else if (CatTop[i].equals(CatTop[j]) && !CatSub[i].equals(CatSub[j])){
                        res[k] = 0;
                    }
                    else{
                        res[k] = 0.028;
                    }
                }
                k+=1;
            }
        }
        return res;
    }
    // 图像2048维特征余弦距离
    public double[] getCosineDist(ArrayList<Float[]> featureList, int n, int featureLen){
        double[] res = new double[n * (n-1) / 2];
        int i,j,k=0;
        for (i=0; i<n-1; i++){
            for (j=i+1; j<n; j++){
                res[k] = 1 - cosineDist(featureList.get(i), featureList.get(j), featureLen, 4);
                k+=1;
            }
        }
        return res;
    }
    // 图像排序中的shopname距离
    public double[] getShopnameDist(ArrayList<Integer> X, int n){
        double[] res = new double[n * (n-1) / 2];
        int i,j,k=0;
        for (i=0; i<n-1; i++){
            for (j=i+1; j<n; j++){
                res[k] = shopenameDist(X.get(i), X.get(j));
                k+=1;
            }
        }
        return res;
    }
    // 编辑距离相似比
    private double LevenshteinRatio(String str1, String str2){
        int[][] d; // 比较矩阵
        int n = str1.length();
        int m = str2.length();
        int i, j;
        char ch1, ch2;
        int tmp; // 记录相同字符在矩阵位置的增量，0/1
        if (n==0) {
            return 0.0;
        }
        if (m==0) {
            return 0.0;
        }
        d = new int[n+1][m+1];
        for (i=0; i<n; i++){ // 初始化第一列
            d[i][0] = i;
        }
        for (j=0; j<m; j++){ // 初始化第一行
            d[0][j] = j;
        }
        for (i=1; i<n+1; i++){
            ch1 = str1.charAt(i-1);
            for (j=i; j<m+1; j++){
                ch2 = str2.charAt(j-1);
                if (ch1 == ch2){
                    tmp = 0;
                } else{
                    tmp = 1;
                }
                d[i][j] = min(d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1]+tmp);
            }
        }
        double strLenMax = Math.max(n, m);
        return d[n][m]/strLenMax;

    }
    private static int min(int one,int two,int three){
        int min = one;
        if (two < min){
            min = two;
        }
        if (three <min){
            min = three;
        }
        return min;
    }
    // 两个set之间的相似度，返回 len(A | B) / len(A & B)
    private double stringSetDist(Set<String> set1, Set<String> set2){
        double n, m;

        // 交集
        Set<String> result = new HashSet<String>(set1);
        result.retainAll(set2);
        n = result.size();

        // 并集
        result.clear();
        result.addAll(set1);
        result.addAll(set2);
        m = result.size();

        return n/m;
    }
    // 求余弦相似度
    private double cosineDist(Float[] vec1, Float[] vec2, int length, int nSubsample) {
        double resultPointMulti = 0;
        double resultSqrt;
        double sqrs1 = 0;
        double sqrs2 = 0;
        for (int i=0; i<length; i+=nSubsample){
            // 点乘
            resultPointMulti += (vec1[i] * vec2[i]);
            // 平方和
            sqrs1 += (vec1[i] * vec1[i]);
            sqrs2 += (vec2[i] * vec2[i]);
        }
        // 开方
        resultSqrt = Math.sqrt(sqrs1 * sqrs2);
        return resultPointMulti / resultSqrt;
    }
    // shopname 距离（需判断是否为null）
    private double shopenameDist(Integer shopname1, Integer shopname2){
        if (shopname1==null && shopname2==null){
            return 0;
        }
        else if (shopname1==null || shopname2==null){
            return 1;
        }
        else if (shopname1.equals(shopname2)){
            return 1;
        }
        else {
            return 0;
        }
    }
    public static void main(String[] args) {
        String x = null;
        Object y = null;
        System.out.println(((Integer)null));
    }

}
