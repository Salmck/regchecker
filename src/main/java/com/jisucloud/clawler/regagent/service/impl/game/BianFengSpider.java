package com.jisucloud.clawler.regagent.service.impl.game;


import com.jisucloud.clawler.regagent.interfaces.PapaSpider;
import com.jisucloud.clawler.regagent.interfaces.PapaSpiderConfig;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;



@Slf4j
@PapaSpiderConfig(
		home = "bianfeng.com", 
		message = "边锋游戏官网是国内比较权威的棋牌游戏平台之一,拥有多款火爆热门棋牌游戏:掼蛋,杭州麻将,安徽斗地主,牛牛等,并且提供免费官方下载。", 
		platform = "bianfeng", 
		platformName = "边锋游戏", 
		tags = { "游戏" }, 
		testTelephones = { "18779861101", "18779861102" })
public class BianFengSpider extends PapaSpider {

	

	public boolean checkTelephone(String account) {
		try {
			String url = "http://cas.bianfeng.com/authen/checkAccountType.jsonp?callback=checkAccountType_JSONPMethod&serviceUrl=register.bianfeng.com&appId=800000345&areaId=0&authenSource=2&inputUserId="+account+"&locale=zh_CN&productId=1&productVersion=1.7&version=21&_=" + System.currentTimeMillis();
			Request request = new Request.Builder().url(url)
					.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0")
					.addHeader("Host", "cas.bianfeng.com")
					.addHeader("Referer", "http://register.bianfeng.com/register/index?appId=800000345")
					.build();
			Response response = okHttpClient.newCall(request).execute();
			String res = response.body().string();
			if (res.contains("existing\": 1")) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean checkEmail(String account) {
		return false;
	}

	@Override
	public Map<String, String> getFields() {
		return null;
	}

}
