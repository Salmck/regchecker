package com.jisucloud.clawler.regagent.service.impl.life;

import com.deep007.goniub.selenium.mitm.ChromeAjaxHookDriver;
import com.google.common.collect.Sets;
import com.jisucloud.clawler.regagent.interfaces.PapaSpider;
import com.jisucloud.clawler.regagent.interfaces.PapaSpiderConfig;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

import org.openqa.selenium.WebElement;

@Slf4j
@PapaSpiderConfig(
		home = "meituan.com", 
		message = "美团网精选美食餐厅,酒店预订,电影票,旅游景点,外卖订餐,团购信息,您可查询商家评价店铺信息。生活,下载美团官方APP ,吃喝玩乐1折起。", 
		platform = "meituan", 
		platformName = "美团网", 
		tags = { "o2o", "外卖", "电影票" , "酒店" , "共享单车" }, 
		testTelephones = { "18212345678", "18210530000" },
		exclude = true)
public class MeiTuanSpider2 extends PapaSpider {
	
	private ChromeAjaxHookDriver chromeDriver;
	private boolean checkTel = false;

	@Override
	public boolean checkTelephone(String account) {
		try {
			chromeDriver = ChromeAjaxHookDriver.newChromeInstance(true, false);
			chromeDriver.get("https://passport.meituan.com/useraccount/retrievepassword?risk_partner=0&service=www&continue=https%3A%2F%2Fwww.meituan.com%2Faccount%2Fsettoken%3Fcontinue%3Dhttps%253A%252F%252Fbj.meituan.com%252F");
			smartSleep(2000);
			WebElement nameInputArea = chromeDriver.findElementByCssSelector("input[class='user-input']");
			nameInputArea.clear();
			nameInputArea.sendKeys(account);
			chromeDriver.findElementByClassName("next-step-btn").click();
			smartSleep(6000);
			for (int i = 0 ; i < 3 ; i++) {
				WebElement rdsSlideReset = chromeDriver.findElementById("yodaBoxWrapper");
				WebElement rdsSlideBtn = chromeDriver.findElementById("yodaBox");
				chromeDriver.switchSlide(rdsSlideReset, rdsSlideBtn);
				smartSleep(RANDOM.nextInt(3000));
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (chromeDriver != null) {
				chromeDriver.quit();
			}
		}
		return checkTel;
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
