package com.xsoft.weibo;

import com.sun.istack.internal.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author riemann
 * @date 2019/05/24 23:42
 */
public class HttpURLConnectionUtil {
    public static String httpGet(String vurl, HashMap<String, Object> map, String body, String setRequestMethod) {
        try {
            URL url = new URL(vurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(setRequestMethod);

            connection.setDoInput(true);// 设置是否从HttpURLConnection输入，默认值为 true
            connection.setDoOutput(true);// 设置是否使用HttpURLConnection进行输出，默认值为 false

            for (Map.Entry item : map.entrySet()) {
                connection.setRequestProperty(item.getKey().toString(),item.getValue().toString());//设置header
            }

            connection.connect();

            if (body != null) {
                OutputStreamWriter writer = new OutputStreamWriter (connection.getOutputStream(), "UTF-8");
                writer.write (body);
                writer.close();
            }

            InputStream in = connection.getInputStream();

            String contentEncoding = connection.getContentEncoding();
//            System.out.println ("contentEncoding = " + contentEncoding);

            if (contentEncoding!=null) {
                if (contentEncoding.equalsIgnoreCase ("gzip")) {
                    in = new GZIPInputStream (in); //解决gzip乱码问题，weibo不需要
                }
            }

            InputStreamReader isr = new InputStreamReader(in, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            isr.close();
            in.close();
            connection.disconnect ();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Http get请求
     *
     * @param httpUrl 连接
     * @return 响应数据
     */
    public static String doGet(String httpUrl, HashMap<String, Object> map) {
        //链接
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        StringBuffer result = new StringBuffer();
        try {
            //创建连接
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();

            for (Map.Entry item : map.entrySet()) {
                connection.setRequestProperty(item.getKey().toString(),item.getValue().toString());//设置header
            }

            //设置请求方式
            connection.setRequestMethod("GET");
            //设置连接超时时间
            connection.setReadTimeout(15000);
            //开始连接
            connection.connect();
            //获取响应数据
            if (connection.getResponseCode() == 200) {
                //获取返回的数据
                is = connection.getInputStream();
                if (null != is) {
                    br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String temp = null;
                    while (null != (temp = br.readLine())) {
                        result.append(temp);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //关闭远程连接
            connection.disconnect();
        }
        return result.toString();
    }

    /**
     * Http post请求
     *
     * @param httpUrl 连接
     * @param param   参数
     * @return
     */
    public static String doPost(String httpUrl, @Nullable String param) {
        StringBuffer result = new StringBuffer();
        //连接
        HttpURLConnection connection = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        try {
            //创建连接对象
            URL url = new URL(httpUrl);
            //创建连接
            connection = (HttpURLConnection) url.openConnection();
            //设置请求方法
            connection.setRequestMethod("POST");
            //设置连接超时时间
            connection.setConnectTimeout(15000);
            //设置读取超时时间
            connection.setReadTimeout(15000);
            //DoOutput设置是否向httpUrlConnection输出，DoInput设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个
            //设置是否可读取
            connection.setDoOutput(true);
            connection.setDoInput(true);
            //设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");

            //拼装参数
            if (null != param && param.equals("")) {
                //设置参数
                os = connection.getOutputStream();
                //拼装参数
                os.write(param.getBytes("UTF-8"));
            }
            //设置权限
            //设置请求头等
            //开启连接
            //connection.connect();
            //读取响应
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                if (null != is) {
                    br = new BufferedReader(new InputStreamReader(is, "GBK"));
                    String temp = null;
                    while (null != (temp = br.readLine())) {
                        result.append(temp);
                        result.append("\r\n");
                    }
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭连接
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //关闭连接
            connection.disconnect();
        }
        return result.toString();
    }

    public static String downloadFile(String fileUrl,String saveUrl) {
        HttpURLConnection httpUrl = null;
        byte[] buf = new byte[1024];
        int size = 0;
        try {
            //下载的地址
            URL url = new URL(fileUrl);
            //支持http特定功能
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            //缓存输入流,提供了一个缓存数组,每次调用read的时候会先尝试从缓存区读取数据
            BufferedInputStream bis = new BufferedInputStream(httpUrl.getInputStream());
            File file = new File(saveUrl);
            //判断文件夹是否存在
            if (!file.exists()) {
                file.createNewFile();
            }
            //输出流,输出到新的地址上面
            FileOutputStream fos = new FileOutputStream(file);
            while ((size = bis.read(buf)) != -1) {
                fos.write(buf, 0, size);
            }
            //记得及时释放资源
            fos.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpUrl.disconnect();
        return null;
    }
}

