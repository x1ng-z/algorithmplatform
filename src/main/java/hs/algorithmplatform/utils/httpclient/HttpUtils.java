package hs.algorithmplatform.utils.httpclient;


import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/8/29 13:57
 * http，数据
 */


public class HttpUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static CloseableHttpClient httpclient = HttpClients.createDefault();

    /**
     * @param url
     * @param data
     */
    public static String PostData(String url, Map<String, String> data) {
        CloseableHttpResponse response2 = null;
        String result = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();

//            nvps.add(new BasicNameValuePair("modleid", algorithmId + ""));

            if (data != null) {
                for (Map.Entry<String, String> datas : data.entrySet()) {
                    nvps.add(new BasicNameValuePair(datas.getKey(), datas.getValue()));
                }
            }

            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            response2 = httpclient.execute(httpPost);
            logger.info(url+(data!=null?data.toString():""));
            logger.info("response code=" + response2.getStatusLine().getStatusCode());
            HttpEntity entity2 = response2.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            result = EntityUtils.toString(entity2);
//            logger.debug("ssss:" + EntityUtils.toString(entity2));
            EntityUtils.consume(entity2);
            return result;

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (response2 != null) {
                try {
                    response2.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }

        }

        return result;
    }


    public static String doget(String url, Map<String, String> map) {
        String result = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建参数队列
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            params.add(new BasicNameValuePair(mapKey, mapValue));
        }
        try {
            //参数转换为字符串
            String paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(params, "UTF-8"));
            url = url + "?" + paramsStr;
            // 创建httpget.
            HttpPost httpget = new HttpPost(url);
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    result = EntityUtils.toString(entity);
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public static void main(String[] args) throws Exception {


        String pointinfo =HttpUtils.PostData("http://192.168.99.219:8070"+"/pointoperate/getalpoints",null);

        System.out.println(pointinfo);



        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://127.0.01./homepage");
        CloseableHttpResponse response1 = httpclient.execute(httpGet);
// The underlying HTTP connection is still held by the response object
// to allow the response content to be streamed directly from the network socket.
// In order to ensure correct deallocation of system resources
// the user MUST call CloseableHttpResponse#close() from a finally clause.
// Please note that if response content is not fully consumed the underlying
// connection cannot be safely re-used and will be shut down and discarded
// by the connection manager.
        try {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(entity1);
        } finally {
            response1.close();
        }


    }


}
