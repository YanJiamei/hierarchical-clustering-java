package com.apporiented.algorithm.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextSorting implements DataSorting{
    private static final String CVID = "cvid";
    private static final String CATEGORY_TOP_ID = "categoryTopId";
    private static final String CATEGORY_SUB_ID = "categorySubId";
    private static final String REVIEW_BODY = "reviewBody";
    private static final String EFFECT_RULE_ID = "effectRuleId";

    @Override
    public int[] getSortedData(List<Map<String, Object>> mapList, int n) throws Exception {
        ArrayList<Integer> cvid = new ArrayList<Integer>();
        ArrayList<Integer> catTop = new ArrayList<Integer>();
        ArrayList<Integer> catSub = new ArrayList<Integer>();
        ArrayList<String> reviewbody = new ArrayList<String>();
        ArrayList<String> effectRuleId = new ArrayList<String>();
        for(Map<String, Object> map : mapList){
            /* Argument checks */
            if (map.get(CVID) == null) {
                throw new IllegalArgumentException("cvid can't be null");
            }
            cvid.add((Integer)map.get(CVID));
            catTop.add((Integer)map.get(CATEGORY_TOP_ID)); //可能为null
            catSub.add((Integer)map.get(CATEGORY_SUB_ID)); //可能为null
            reviewbody.add(map.get(REVIEW_BODY)==null ? "":map.get(REVIEW_BODY).toString()); //可能为null，需转换
            effectRuleId.add(map.get(EFFECT_RULE_ID)==null ? "":map.get(EFFECT_RULE_ID).toString()); //可能为null，需转换
        }

        MixedPDist mixedPDist = new MixedPDist();
        double[] pDist = mixedPDist.getTxtPDist(catTop, catSub, reviewbody, effectRuleId, n);

        ClusterSorting clusterSorting = new ClusterSorting();
        int[] sortedId = clusterSorting.Sorting(pDist, n);
        HashMap<Integer, Integer> resultMap = new HashMap<>();
        for (int i = 0; i < n; i++){
            resultMap.put(cvid.get(i), sortedId[i]);
        }
        return sortedId;
    }
}
