package com.xsoft.weibo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;


public class Weibo {
    private final String WeiBo_URL = "https://weibo.com/ajax/statuses/mymblog?uid=";
    private final String ORG_Weibo_URL = "https://m.weibo.cn/status/";
    private final String referer_URL = "https://weibo.com/u/";
    private final String CONFIG_PATH = "C:\\weibo_config\\weibo_uid.txt";
    private final String TEMP_PATH = "C:\\weibo_temp\\";

    //pic_ids pic_infos bmiddle url
    private final long WaitTime = 5000;

    private final String x_xsrf_token = "NmHZBcNKvDAqc9wQ5UzXVLts";
    private final String cookie = "XSRF-TOKEN=NmHZBcNKvDAqc9wQ5UzXVLts; _s_tentry=weibo.com; Apache=853668190835.6503.1658559738138; SINAGLOBAL=853668190835.6503.1658559738138; login_sid_t=82060b6f88cdfd20118cdea509275098; cross_origin_proto=SSL; SSOLoginState=1658594857; PC_TOKEN=d2133c2808; WBStorage=4d96c54e^|undefined; wb_view_log=1920*10801; UOR=passport.weibo.com,weibo.com,login.sina.com.cn; ULV=1692974143938:1:1:1:853668190835.6503.1658559738138:; SCF=ArIS_fuPjWNeOyGAphE3XXB3R8nbNAY10nn3-s8aoGBIMODVw6o2w9GVZ2_bybNBQuvYRh-xgBxD7RnmmkOX-50.; SUB=_2A25J7MzGDeRhGedG7lcX9S3IyzmIHXVqm7kOrDV8PUNbmtANLRLgkW9NUTfRvU4H272Hs6kNkPmxui5Rst0H0xMN; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WFe8cjIjD9MRJqGCDqCiI2E5JpX5KzhUgL.Fo2RSK-cSKeXeh-2dJLoIEMLxKBLBonL1-eLxKqLBo-LBoMLxK-L1hzLBozLxKqL1-BLBK-pehzt; ALF=1724510229; WBPSESS=ysQuCA-kQ7ttwGPrDclot3ACRxQWN4cEzUQK5Q8V1E_3vPhKqhB4YSqcUjgIprGlON0iLMu1qZbPrGOoCFNZmymvyOc7YS7SaoBtLHDnzY4q2xrS_b30iDNWRCFhHef0FLIzvIWeuE0HExuerMalvQ==";
    private List<String> weiboUidList = new ArrayList<>();
    private List<String> savePicList = new ArrayList<>();
    private boolean isDebug = false;

    public void run() {
        if (isDebug) {
//            weiboUidList.add("2198436847");
//            weiboUidList.add("6225121051");
//            weiboUidList.add("1987241375");
//            weiboUidList.add("1917872472");
//            weiboUidList.add("5748988380");
//            weiboUidList.add("3194506490");
//            weiboUidList.add("5756956812");
//            weiboUidList.add("6744915183");
//            weiboUidList.add("5120842149");
            weiboUidList.add("5675300697");
//            weiboUidList.add("5958505676");
        } else {
            parseWeiboUID();
        }

        while (true) {
            for (String uid : weiboUidList) {
                String jsonStr = getWeiboMsgByUID(uid);
                String[] data = getWeiboData(jsonStr);
                boolean isOK = savePicData(data[0], uid);
                isOK &= writeData(data[1], uid);
                //System.out.println("jsonStr = " + jsonStr);
                System.out.println("data = " + data[0]);
                System.out.println("data = " + data[1]);
                System.out.println("isOK = " + isOK);
                try {
                    Thread.sleep(WaitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseWeiboUID() {
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

    private boolean writeData(String str, String uid) {
        if (isDebug) return false;
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

    private boolean savePicData(String str, String uid) {
        if (isDebug) return false;
        String fileName = TEMP_PATH + uid + "_pic.txt";
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

    private int getNewPublicTimeIndex(JSONArray jsonArray) {
        int maxIndex = 0;
        long maxTime = 0;

        int size = jsonArray.size ();
        for(int i=0; i<size; i++) {
            String timeStr = jsonArray.getJSONObject(i).getString ("created_at");
            long time = getTime (timeStr);
            if (time > maxTime) {
                maxTime = time;
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    private long getTime(String timeStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        try {
            Date date = simpleDateFormat.parse(timeStr);
            long ts = date.getTime();
            return ts;
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return 0;
    }

    private String[] getWeiboData(String json) {
        JSONObject jsonObject = JSON.parseObject(json);
        String text = "";
        long id = 0;
        String textAndOrgURL = "";
        String picPath="";
        try {
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
            int newPublishIndex = getNewPublicTimeIndex (jsonArray);

            text = jsonArray.getJSONObject(newPublishIndex).getString("text_raw");
            text = text.replace ('\u200B', ' ');

            id = jsonArray.getJSONObject(newPublishIndex).getLong("id");

            JSONArray pic_idsArray = jsonArray.getJSONObject(newPublishIndex).getJSONArray("pic_ids");
            int size = pic_idsArray.size();
            for(int i=0; i<size; i++) {
                String pic_id = pic_idsArray.getString(i);
                String pic_url = jsonArray.getJSONObject(newPublishIndex).getJSONObject("pic_infos")
                        .getJSONObject(pic_id).getJSONObject("bmiddle").getString("url");

                //保存图片
                String pic_filename = pic_url.substring (pic_url.lastIndexOf ("/") + 1);
                String pic_path = TEMP_PATH + "pic\\" + pic_filename;
                String pic_tmp_path = TEMP_PATH + "pic_tmp\\" + pic_filename;

                //把后缀都改成 jpg
//                String last = pic_path.substring (pic_path.lastIndexOf (".") + 1);
//                if (!last.equals ("jpg")) {
//                    System.out.println ("变更名字前 " + pic_path);
//
//                    String qian = pic_path.substring (0, pic_path.lastIndexOf ("."));
//                    pic_path = qian + ".jpg";
//
//                    System.out.println ("变更名字后 " + pic_path);
//                }

                File pic_file = new File (pic_path);
                File pic_fileTemp = new File (pic_tmp_path);
                if (!isDebug) {
                    if (!savePicList.contains (pic_path)) {
                        if (!pic_file.exists ()) {
                            HttpURLConnectionUtil.downloadFile (pic_url, pic_tmp_path);
                            pic_fileTemp.renameTo (pic_file);
                            savePicList.add (pic_path);
                        }
                    }
                }

                String width = jsonArray.getJSONObject(newPublishIndex).getJSONObject("pic_infos")
                        .getJSONObject(pic_id).getJSONObject("bmiddle").getString("width");

                String height = jsonArray.getJSONObject(newPublishIndex).getJSONObject("pic_infos")
                        .getJSONObject(pic_id).getJSONObject("bmiddle").getString("height");

                String md5 = getMD5Checksum(pic_url);

//                textAndOrgURL = textAndOrgURL + "[pic,hash=" + md5 + ",url=" + pic_url
//                        + ",wide=" + width + ",high=" + height + ",cartoon=false" + "]\n";

                picPath = picPath + pic_filename + "\n";
            }

            textAndOrgURL = text + "\n 【微博地址】" + ORG_Weibo_URL + id;
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] strings = new String[]{picPath, textAndOrgURL};

        return strings;
    }

    private String getWeiboMsgByUID(String uid) {
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

        String vmsg= HttpURLConnectionUtil.httpGet(requestURL,tmap, null, "GET");//获取请求的返回结果

        return vmsg;
    }

    private byte[] createChecksum(String url) throws Exception {
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

    private String getMD5Checksum(String url) throws Exception {
        byte[] b = createChecksum(url);

        String result = "";

        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring(1);//加0x100是因为有的b[i]的十六进制只有1位

        }

        return result;

    }
}
