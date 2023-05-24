package com.xsoft.weibo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaobaoAlliance {
    private final String API_URL = "https://pub.alimama.com/openapi/param2/1/gateway.unionpub/union.pub.entry";
    private final String URL = "https://pub.alimama.com/portal/v2/pages/promo/goods/index.htm?pageNum=1&pageSize=30&filters=%257B%2522usertype%2522%253A%25221%2522%252C%2522service%2522%253A%2522dpyhq%2522%252C%2522max_tk_rate_filter%2522%253A10%252C%2522coupon_amount_filter%2522%253A10%252C%2522dsr%2522%253A%25224.8%2522%257D&fn=search&q=&sort=default&selected=%257B%2522usertype%2522%253A%25221%2522%252C%2522service%2522%253A%2522dpyhq%2522%252C%2522max_tk_rate_filter%2522%253A%25221000~%2522%252C%2522coupon_amount_filter%2522%253A%252210~%2522%252C%2522dsr%2522%253A%252248000~%2522%257D&floorId=61354";
    private final String cookie2_alimama = "18ce29b5b0baae7439f27f10b318e479";
    private final String _tb_token_ = "e3597645e15a3";
    private final int pageSize = 60;
    private final float discount_rate = 0.1f;
    private boolean isDebug = true;
    private int totalCount = 0;
    private int pageCount = 1000;
    private int pageIndex = 0;

    public void run() {
        while (pageIndex < pageCount) {
            System.out.println ("======= pageIndex = " + pageIndex + " =======");

            String pageJson = getTaobaoMsgByPage (pageIndex);
//            if (isDebug) System.out.println ("pageJson = " + pageJson);

            getCommodityInfo(pageJson);

            try {
                Thread.sleep (5000);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
            pageIndex++;
        }
    }

    private List<CommodityInfo> getCommodityInfo(String json) {
        List<CommodityInfo> commodityInfos = new ArrayList ();
        JSONObject jsonObject = JSON.parseObject(json);
        JSONObject modelJSONObj = jsonObject.getJSONObject ("model");

        if (totalCount == 0) {
            totalCount = modelJSONObj.getIntValue ("totalCount");
            pageCount = totalCount/pageSize;
            if (totalCount > pageCount*pageSize) {
                pageCount++;
            }

            System.out.println ("totalCount = " + totalCount + ", pageCount = " + pageCount);
        }

        JSONArray jsonArray = modelJSONObj.getJSONArray("resultList");
        int size = jsonArray.size();

        for (int i=0; i<size; i++) {
            JSONObject itemJsonObj = jsonArray.getJSONObject (i);
            CommodityInfo commodityInfo = new CommodityInfo ();
            commodityInfo.shortTitle = itemJsonObj.getString ("shortTitle");
            commodityInfo.price = itemJsonObj.getString ("price");
            commodityInfo.priceAfterCoupon = itemJsonObj.getString ("priceAfterCoupon");
            commodityInfo.monthSellCount = itemJsonObj.getString ("monthSellCount");
            commodityInfo.couponEffectiveStartTime = itemJsonObj.getString ("couponEffectiveStartTime");
            commodityInfo.couponEffectiveEndTime = itemJsonObj.getString ("couponEffectiveEndTime");
            commodityInfo.whiteImage = itemJsonObj.getString ("whiteImage");
            commodityInfo.itemId = itemJsonObj.getString ("itemId");
            commodityInfo.url = "https:" + itemJsonObj.getString ("url");

            commodityInfo.changeDataFormat();

            float itemDiscountRate = commodityInfo.priceAfterCoupon_float/commodityInfo.price_float;
            if (itemDiscountRate <= discount_rate) {
                System.out.println (commodityInfo);
                commodityInfos.add (commodityInfo);
            }
        }

        return commodityInfos;
    }

    private String getTaobaoMsgByPage(int pagenum) {
        if (isDebug) System.out.println ("pagenum = " + pagenum);
        String requestURL = Utils.replaceValueByKey (URL, "pageNum", pagenum+"");
        if (isDebug) System.out.println ("requestURL = " + requestURL);

        HashMap<String, Object> tmap = new HashMap<String, Object>();
        tmap.put("Host", "pub.alimama.com");
        tmap.put("Cookie", "cookie2_alimama="+cookie2_alimama+"; _tb_token_="+_tb_token_+"");
        tmap.put("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        tmap.put("x-requested-with", "XMLHttpRequest");
        tmap.put("bx-v", "2.2.3");
        tmap.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
        tmap.put("origin", "https://pub.alimama.com");
        tmap.put("referer", requestURL);

        String body = getTaobaoBodyByPage(pagenum);
        if (isDebug) System.out.println ("body = " + body);
        return HttpURLConnectionUtil.httpGet (API_URL,tmap, body);
    }

    private String getTaobaoBodyByPage (int index) {
        String body = "t=1684833495655&_tb_token_="+_tb_token_+"&bizType=pub.smartNavigator&bizParam={\"sceneCode\":\"app_smart_navigator\",\"floorId\":61354,\"pageNum\":"+index+",\"pageSize\":\""+pageSize+"\",\"pid\":\"mm_121185295_0_0\",\"variableMap\":{\"fn\":\"search\",\"resultCanBeEmpty\":true,\"q\":\"\",\"curSelected\":{},\"pubFloorId\":61354,\"sort\":\"default\",\"usertype\":\"1\",\"service\":\"dpyhq\",\"max_tk_rate_filter\":\"1000~\",\"coupon_amount_filter\":\"10~\",\"dsr\":\"48000~\",\"tk_navigator\":\"true\",\"union_lens\":\"b_pvid:a219t._portal_v2_pages_promo_goods_index_htm_1684833495654_4901468619305387_ /DAF\",\"lensScene\":\"PUB\",\"spmB\":\"_portal_v2_pages_promo_goods_index_htm\"}}";
        return body;
    }
}
