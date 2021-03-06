package com.jisucloud.clawler.regagent.service.impl.borrow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;

import com.jisucloud.clawler.regagent.interfaces.PapaSpider;
import com.jisucloud.clawler.regagent.interfaces.PapaSpiderConfig;

import java.util.HashMap;
import java.util.Map;


@PapaSpiderConfig(
		home = "xicaimao.cn", 
		message = "喜财猫是中国领先的互联网科技平台,致力于安全高效的贷款服务。20万信贷经理24小时为您提供个人贷款、企业贷款、小额贷款、银行贷款、无抵押贷款.", 
		platform = "xicaimao", 
		platformName = "喜财猫", 
		tags = { "P2P", "消费分期" , "借贷" }, 
		testTelephones = { "18210538577", "18212345678" })
@Slf4j
public class XicaiMaoSpider extends PapaSpider {
    
    private Headers getHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "okhttp/3.10.0");
        headers.put("Host", "passport.csaimall.com");
        headers.put("referer", "api.xicaimao.cn");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("version", "3.0.2");
        headers.put("versionCode", "32");
        headers.put("handleType", "Android");
        headers.put("phoneModel", "4.4.2");
        headers.put("equipType", "ANDROID");
        headers.put("cstk", "noLogin");
        return Headers.of(headers);
    }
    
    
    public boolean checkTelephone(String account) {
        try {
        	FormBody formBody = new FormBody
	                .Builder()
	                .add("mobileIden", "ffffffff-feb1-ed81-ffff-ffffc7c725a4")
                    .add("userName", account)
                    .add("password", "93B055F731F9ABC6BB4DCC17A8B7131B")
                    .add("mobileType", "8692-A00")
	                .build();
        	Request request = new Request.Builder().url("https://passport.csaimall.com/app/user/login.do")
        			.headers(getHeader())
					.post(formBody)
					.build();
            Response response =okHttpClient.newCall(request).execute();
            if (response != null) {
                JSONObject result = JSON.parseObject(response.body().string());
                if (result.getString("resultMsg").equals("用户名或密码错误")) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    
    public boolean checkEmail(String account) {
        return checkTelephone(account);
    }

    
    public Map<String, String> getFields() {
        return null;
    }

}
