package com.xsoft.weibo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    static final String WeiBo_URL = "https://weibo.com/ajax/statuses/mymblog?uid=";
    static final String ORG_Weibo_URL = "https://m.weibo.cn/status/";
    static final String referer_URL = "https://weibo.com/u/";
    static final String CONFIG_PATH = "C:\\weibo_config\\weibo_uid.txt";
    static final String TEMP_PATH = "C:\\weibo_temp\\";

    //pic_ids pic_infos bmiddle url
    static final long WaitTime = 1*60000;

    static final String x_xsrf_token = "NmHZBcNKvDAqc9wQ5UzXVLts";
    static final String cookie = "PC_TOKEN=2ed96e54a1; SUB=_2AkMVhxWbf8NxqwJRmfsQy2nlao5zyQHEieKj2-RAJRMxHRl-yT9jqkNatRB6Pgc7dASpZQCTumL0RPTq_h1fDg1ggv8z; SUBP=0033WrSXqPxfM72-Ws9jqgMF55529P9D9WWOe5WOd6biMhsCKGGkun8I; XSRF-TOKEN=NmHZBcNKvDAqc9wQ5UzXVLts; WBPSESS=mm07v0uQ8nV44TNSi6a9LQYvQnUzySLJhc3SLeYGHOmt4eqSj_Txsi-5LERlCcwtjyvouSWA2UOfDz9h36nl5927B5LB8BsIy5p5NBv77z8iodciks3JRgFv0hzw9C4f1_7RU7Vh-7-M-GWvORRTz6Ntp9Df44FeRTdk4q8XMTg=";
    static List<String> weiboUidList = new ArrayList<>();
    static List<String> savePicList = new ArrayList<>();

    public static void main(String[] args) {
        parseWeiboUID();

        while (true) {
            for (String uid : weiboUidList) {
                String jsonStr = getWeiboMsgByUID(uid);
                String data = getWeiboData(jsonStr);
                boolean isOK = writeData(data, uid);
                //System.out.println("jsonStr = " + jsonStr);
                System.out.println("data = " + data);
                System.out.println("isOK = " + isOK);
                try {
                    Thread.sleep(WaitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void parseWeiboUID() {
        try {
            BufferedReader in = new BufferedReader
                    (new FileReader(CONFIG_PATH));
            String str;
            while ((str = in.readLine()) != null) {
                weiboUidList.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean writeData(String str, String uid) {
        String fileName = TEMP_PATH + uid + ".txt";
        File file = new File(fileName);
        boolean isOK = false;

        try {
            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "GBK");
            out.write(str);
            out.flush();
            out.close();
            isOK = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isOK;
    }

    private static String getWeiboData(String json) {
        JSONObject jsonObject = JSON.parseObject(json);
        String text = "";
        long id = 0;
        String textAndOrgURL = "";
        try {
            text = jsonObject.getJSONObject("data").getJSONArray("list").getJSONObject(0).getString("text_raw");
            if (text.length() > 3) {
                text = text.substring(0, text.length()-3);
            }

            id = jsonObject.getJSONObject("data").getJSONArray("list").getJSONObject(0).getLong("id");

            JSONArray pic_idsArray = jsonObject.getJSONObject("data").getJSONArray("list").getJSONObject(0).getJSONArray("pic_ids");
            int size = pic_idsArray.size();
            for(int i=0; i<size; i++) {
                String pic_id = pic_idsArray.getString(i);
                String pic_url = jsonObject.getJSONObject("data").getJSONArray("list")
                        .getJSONObject(0).getJSONObject("pic_infos")
                        .getJSONObject(pic_id).getJSONObject("bmiddle").getString("url");

                //保存图片
                String pic_filename = pic_url.substring(pic_url.lastIndexOf("/")+1);
                String pic_path = TEMP_PATH + "pic\\" + pic_filename;
                File pic_file = new File(pic_path);
                if(!savePicList.contains(pic_path)) {
                    if (!pic_file.exists()) {
                        HttpURLConnectionUtil.downloadFile(pic_url, pic_path);
                        savePicList.add(pic_path);
                    }
                }

                String width = jsonObject.getJSONObject("data").getJSONArray("list")
                        .getJSONObject(0).getJSONObject("pic_infos")
                        .getJSONObject(pic_id).getJSONObject("bmiddle").getString("width");

                String height = jsonObject.getJSONObject("data").getJSONArray("list")
                        .getJSONObject(0).getJSONObject("pic_infos")
                        .getJSONObject(pic_id).getJSONObject("bmiddle").getString("height");

                String md5 = getMD5Checksum(pic_url);

                textAndOrgURL = textAndOrgURL + "[pic,hash=" + md5 + ",url=" + pic_url
                        + ",wide=" + width + ",high=" + height + ",cartoon=false" + "]\n";
            }

            textAndOrgURL = textAndOrgURL + text + "\n 【微博地址】" + ORG_Weibo_URL + id;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return textAndOrgURL;
    }

    private static String getWeiboMsgByUID(String uid) {
        String requestURL = WeiBo_URL + uid;

        HashMap<String, Object> tmap = new HashMap<String, Object>();
        tmap.put("authority", "weibo.com");
        tmap.put("accept", "application/json, text/plain, */*");
        tmap.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,ja;q=0.7,zh-TW;q=0.6,und;q=0.5,la;q=0.4");
        tmap.put("client-version", "weibo.com");
        tmap.put("cookie", cookie);
        tmap.put("referer", referer_URL+uid);
        tmap.put("sec-ch-ua", "^\\^\".Not/A)Brand^\\^\";v=^\\^\"99^\\^\", ^\\^\"Google Chrome^\\^\";v=^\\^\"103^\\^\", ^\\^\"Chromium^\\^\";v=^\\^\"103^\\^\"");
        tmap.put("sec-ch-ua-mobile", "?0");
        tmap.put("sec-ch-ua-platform", "^\\^\"Windows^\\^");
        tmap.put("sec-fetch-dest", "empty");
        tmap.put("sec-fetch-mode", "cors");
        tmap.put("sec-fetch-site", "same-origin");
        tmap.put("server-version", "v2022.07.20.1");
        tmap.put("traceparent", "00-0c13167913ae6cc4df0a8630e3436c51-0a56f2e6c51a5bdc-00");
        tmap.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
        tmap.put("x-requested-with", "XMLHttpRequest");
        tmap.put("x-xsrf-token", x_xsrf_token);

        String vmsg= HttpURLConnectionUtil.httpGet(requestURL,tmap);//获取请求的返回结果

        return vmsg;
    }

    public static byte[] createChecksum(String url) throws Exception {
        InputStream fis = new URL(url).openStream();; //将流类型字符串转换为String类型字符串

        byte[] buffer = new byte[1024];

        MessageDigest complete = MessageDigest.getInstance("MD5"); //如果想使用SHA-1或SHA-256，则传入SHA-1,SHA-256

        int numRead;

        do {
            numRead = fis.read(buffer); //从文件读到buffer，最多装满buffer

            if (numRead > 0) {
                complete.update(buffer, 0, numRead); //用读到的字节进行MD5的计算，第二个参数是偏移量

            }

        } while (numRead != -1);

        fis.close();

        return complete.digest();

    }

    public static String getMD5Checksum(String url) throws Exception {
        byte[] b = createChecksum(url);

        String result = "";

        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring(1);//加0x100是因为有的b[i]的十六进制只有1位

        }

        return result;

    }
}