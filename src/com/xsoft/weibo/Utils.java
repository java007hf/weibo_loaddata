package com.xsoft.weibo;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    //https://www.cnblogs.com/lanpo/articles/10046662.html

    public static String replaceValueByKey (String url, String key, String value) {
        url = url.replaceAll ("(" + key + "=[^&]*)", key + "=" + value);
        return url;
    }

    /**
     * 获取URL中的参数名和参数值的Map集合
     *
     * @param url
     * @return
     */
    public static Map<String, String> getUrlPramNameAndValue (String url) {
        String regEx = "(\\?|&+)(.+?)=([^&]*)";//匹配参数名和参数值的正则表达式
        Pattern p = Pattern.compile (regEx);
        Matcher m = p.matcher (url);
        Map<String, String> paramMap = new LinkedHashMap<String, String> ();
        while (m.find ()) {
            String paramName = m.group (2);//获取参数名
            String paramVal = m.group (3);//获取参数值
            paramMap.put (paramName, paramVal);
        }
        return paramMap;
    }

    public static String getUrlPramValue (String url, String key) {
        String value = null;
        String regEx = "(\\?|&+)(.+?)=([^&]*)";//匹配参数名和参数值的正则表达式
        Pattern p = Pattern.compile (regEx);
        Matcher m = p.matcher (url);
        Map<String, String> paramMap = new LinkedHashMap<String, String> ();
        while (m.find ()) {
            String paramName = m.group (2);//获取参数名
            String paramVal = m.group (3);//获取参数值

            if (key.equalsIgnoreCase (paramName)) {
                value = paramVal;
                break;
            }
        }
        return value;
    }

    //使用URLEncoder.encode编码
    public static String urlEncode (String urlToken) {
        String encoded = null;

        try {
            //用URLEncoder.encode方法会把空格变成加号(+),encode之后在替换一下
            encoded = URLEncoder.encode (urlToken, "UTF-8").replace ("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace ();
        }
        return encoded;
    }

    //使用URLEncoder.encode解码
    public static String urlDecode(String urlToken) {
        String decoded = null;

        try {
            decoded = URLDecoder.decode(urlToken, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace ();
        }

        return decoded;
    }
}
