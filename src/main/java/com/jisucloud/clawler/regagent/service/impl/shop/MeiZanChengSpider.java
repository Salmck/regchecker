package com.jisucloud.clawler.regagent.service.impl.shop;


import com.jisucloud.clawler.regagent.interfaces.PapaSpider;
import com.jisucloud.clawler.regagent.interfaces.PapaSpiderConfig;

import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;



@Slf4j
@PapaSpiderConfig(
		home = "meadjohnson.com.cn", 
		message = "美赞臣营养品公司创立于1905年，创始人为爱德华·美赞臣,公司总部位于美国伊利诺斯州的格伦维尤，其全球研发中心及全球运作生产中心则位于美国印地安纳州的埃文思威尔镇。美赞臣汇聚前沿的营养科研力量，一直致力于配方的研发和创新。", 
		platform = "meadjohnson", 
		platformName = "美赞臣", 
		tags = { "母婴" }, 
		testTelephones = { "18515290000", "13811085745" })
public class MeiZanChengSpider extends PapaSpider {

	

	public boolean checkTelephone(String account) {
		try {
			String url = "https://www.meadjohnson.com.cn/validate-user-exist";
			FormBody formBody = new FormBody
	                .Builder()
	                .add("cellphone", account)
	                .build();
			Request request = new Request.Builder().url(url)
					.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0")
					.addHeader("Referer", "https://www.meadjohnson.com.cn/register")
					.post(formBody)
					.build();
			Response response = okHttpClient.newCall(request).execute();
			String res = response.body().string();
			return res.contains("err_code\":\"100");
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
