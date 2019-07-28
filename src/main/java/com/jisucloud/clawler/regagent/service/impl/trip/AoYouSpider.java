package com.jisucloud.clawler.regagent.service.impl.trip;

import com.google.common.collect.Sets;
import com.jisucloud.clawler.regagent.interfaces.PapaSpider;
import com.jisucloud.clawler.regagent.interfaces.UsePapaSpider;

import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@UsePapaSpider
public class AoYouSpider extends PapaSpider {

	

	@Override
	public String message() {
		return "遨游网是A股上市公司中青旅旗下在互联网时代打造的旅行生活平台，提供国内/出境游、海岛游、签证办理、机票预订、精品酒店、游学、康养、境外当地玩乐、高端定制等品质旅行服务，移动端品牌为遨游旅行。";
	}

	@Override
	public String platform() {
		return "aoyou";
	}

	@Override
	public String home() {
		return "aoyou.com";
	}

	@Override
	public String platformName() {
		return "遨游网";
	}

	@Override
	public String[] tags() {
		return new String[] {"旅游" , "酒店" , "美食" , "o2o"};
	}
	
	@Override
	public Set<String> getTestTelephones() {
		return Sets.newHashSet("13910252045", "18210538513");
	}

	@Override
	public boolean checkTelephone(String account) {
		try {
			String url = "https://passport.aoyou.com/Reg/VerificationMobile";
			FormBody formBody = new FormBody
	                .Builder()
	                .add("regMobile", account)
	                .build();
			Request request = new Request.Builder().url(url)
					.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0")
					.addHeader("Host", "passport.aoyou.com")
					.addHeader("Referer", "https://passport.aoyou.com/Reg/Index")
					.post(formBody)
					.build();
			Response response = okHttpClient.newCall(request).execute();
			String res = response.body().string();
			if (res.contains("exist\":true") || res.contains("已存在")) {
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