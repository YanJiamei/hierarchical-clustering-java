package com.apporiented.algorithm.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageSorting implements DataSorting {
    private static final String CVID = "cvid";
    private static final String CATEGORY_TOP_ID = "categoryTopId";
    private static final String CATEGORY_SUB_ID = "categorySubId";
    private static final String IMG_FEATURE = "imageFeature";
    private static final String EFFECT_RULE_ID = "effectRuleId";
    private static final String SHOP_ID  = "shopid";

    @Override
    public int[] getSortedData(List<Map<String, Object>> mapList, int n) throws Exception {
        ArrayList<Integer> cvid = new ArrayList<>();
        ArrayList<Integer> catTop = new ArrayList<>();
        ArrayList<Integer> catSub = new ArrayList<>();
        ArrayList<Integer> shopId = new ArrayList<>();
        ArrayList<Float[]> imageFeature = new ArrayList<>();
        ArrayList<String> effectRuleId = new ArrayList<>();

        ArrayList<Integer> cvidExclude = new ArrayList<>();
        int sortedNum = 0;
        for(Map<String, Object> map : mapList){
            /* Argument checks */
            if (map.get(CVID) == null) {
                throw new IllegalArgumentException("cvid can't be null");
            }
            if (map.get(IMG_FEATURE) == null){
                cvidExclude.add((Integer)map.get(CVID));
            }
            else{
                cvid.add((Integer)map.get(CVID));
                catTop.add((Integer)map.get(CATEGORY_TOP_ID)); //可能为null
                catSub.add((Integer)map.get(CATEGORY_SUB_ID)); //可能为null
                shopId.add((Integer)map.get(SHOP_ID)); //可能为null
                effectRuleId.add(map.get(EFFECT_RULE_ID)==null ? "":map.get(EFFECT_RULE_ID).toString()); //可能为null，需转换
                imageFeature.add((Float[]) map.get(IMG_FEATURE));
                sortedNum += 1;
            }
        }

        MixedPDist mixedPDist = new MixedPDist();
        double[] pDist = mixedPDist.getImgPDist(catTop, catSub, shopId, imageFeature , effectRuleId, sortedNum);

        ClusterSorting clusterSorting = new ClusterSorting();
        int[] sortedId = clusterSorting.Sorting(pDist, sortedNum);
        HashMap<Integer, Integer> resultMap = new HashMap<>();
        for (int i=0; i<sortedNum; i++){
            resultMap.put(cvid.get(i), sortedId[i]);
        }
        for (int i = 0; i<n-sortedNum; i++){
            resultMap.put(cvidExclude.get(i), i);
        }
        return new int[0];
    }
}
