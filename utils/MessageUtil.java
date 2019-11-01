package com.bwie.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

public class MessageUtil {

	//1验证码生产方法
	public static  String getYzm() {
		String yzm = "";
		for(int i=0;i<4;i++) {
			int int1 = new Random().nextInt(10);
			yzm+=int1;
		}
		return yzm;
	}
	
	//发送短信方法
	public static   void sendDuanXin(String phone,String yzm) {
	    String host = "https://exempt.market.alicloudapi.com";
	    String path = "/exemptCode";
	    String method = "GET";
	    String appcode = "e01dd9e19fdd4012b9805b3939c7d233";
	    Map<String, String> headers = new HashMap<String, String>();
	    //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
	    headers.put("Authorization", "APPCODE " + appcode);
	    Map<String, String> querys = new HashMap<String, String>();
	    querys.put("content", "【涪擎】您的验证码是：{"+yzm+"}，请在3分钟内使用。请勿泄露。");
	    querys.put("phone", phone);
	    try {
	    	/**
	    	* 重要提示如下:
	    	* HttpUtils请从
	    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
	    	* 下载
	    	*
	    	* 相应的依赖请参照
	    	* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
	    	*/
	    	HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
	    	//System.out.println(response.toString());如不输出json, 请打开这行代码，打印调试头部状态码。
                //状态码: 200 正常；400 URL无效；401 appCode错误； 403 次数用完； 500 API网管错误
	    	//获取response的body
			    	System.out.println(EntityUtils.toString(response.getEntity()));
			    } catch (Exception e) {
			    	e.printStackTrace();
			    }
			}
}
