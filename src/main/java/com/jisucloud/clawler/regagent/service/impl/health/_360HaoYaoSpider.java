package com.jisucloud.clawler.regagent.service.impl.health;


import com.jisucloud.clawler.regagent.interfaces.PapaSpider;
import com.jisucloud.clawler.regagent.interfaces.PapaSpiderConfig;

import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;


@Slf4j
@PapaSpiderConfig(
		home = "360haoyao.com", 
		message = "360健康,是奇虎360旗下医药门户网站,本着全心全意为健康服务的宗旨,提供包括药品查询,疾病查询,症状查询,医院查询,医生查询,挂号,专家咨询,就医攻略。", 
		platform = "360haoyao", 
		platformName = "360健康", 
		tags = { "健康运动", "医疗", "生活应用" , "购药" }, 
		testTelephones = { "13528428484", "18212345678" })
public class _360HaoYaoSpider extends PapaSpider {

	public boolean checkTelephone(String account) {
		try {
			String url = "http://login.360haoyao.com/passport/customer/validateEmail.action";
			FormBody formBody = new FormBody
	                .Builder()
	                .add("email", account)
	                .build();
			Request request = new Request.Builder().url(url)
					.addHeader("User-Agent", CHROME_USER_AGENT)
					.addHeader("Referer", "http://login.360haoyao.com/passport/register.html")
					.post(formBody)
					.build();
			Response response = okHttpClient.newCall(request).execute();
			String res = response.body().string();
			if (res.contains("1")) {
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
