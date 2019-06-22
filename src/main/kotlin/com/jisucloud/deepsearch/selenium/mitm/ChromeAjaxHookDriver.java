package com.jisucloud.deepsearch.selenium.mitm;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import com.jisucloud.deepsearch.selenium.HttpsProxy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChromeAjaxHookDriver extends ChromeDriver implements Runnable{
	
	public static final Random random = new Random();
	
	public static final String hookIdName = "hookName";
	private final String hookIdValue = UUID.randomUUID().toString().replaceAll("\\-", "");
	
	private int hookerNum = 0;
	
	private Thread checkHookJs = null;
	
	private boolean isQuited;
	
	public static final ChromeAjaxHookDriver newInstance(boolean disableLoadImage,boolean headless,HttpsProxy proxy,String userAgent) {
		return new ChromeAjaxHookDriver(ChromeOptionsUtil.createChromeOptions(disableLoadImage, headless, proxy, userAgent));
	}
	
	public ChromeAjaxHookDriver(ChromeOptions options) {
		super(options);
		manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);//脚步执行超时
		manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);//页面加载超时
		checkHookJs = new Thread(this);
		checkHookJs.start();
	}
	
	public void addAjaxHook(AjaxHook hook) {
		if (hook != null) {
			MitmServer.getInstance().addAjaxHook(hookIdValue, hook);
			hookerNum ++;
		}
	}

	@Override
	public void get(String url) {
		for (int i = 0 ; i < 3 ; i++) {
			try {
				super.get(url);
				log.info("visit:"+url);
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
			reInject();
			break;
		}
		reInject();
	}
	
	public boolean isXHRHooked() {
		Object ret = null;
		try {
			String script = "return window.hooked != undefined && window.hookIdName != undefined && window.hookIdValue != undefined;";
			ret = executeScript(script);
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
		}
		return ret != null? (Boolean)ret : false;
	}
	
	public boolean checkElement(String cssSelect) {
		try {
			return findElementByCssSelector(cssSelect).isDisplayed();
		}catch(Exception e) {
		}
		return false;
	}
	
	
	
	private void sleep(long mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void reInject() {
		if (isQuited || hookerNum == 0) {
			return;
		}
		if (!isXHRHooked()) {
			executeScript(AjaxHookJs.getHookAjaxJs(hookIdName, hookIdValue));
			log.info("hookajaxjs inject success");
		}
	}
	
	@Override
	public String getPageSource() {
		String ret = super.getPageSource();
		Matcher matcher = Pattern.compile("<html><head></head>.*wrap;\">").matcher(ret);
		if (matcher.find()){
			ret = ret.replace(matcher.group(), "").replace("</pre></body></html>", "");
		}else if (ret.startsWith("<html><head></head><body>")) {
			ret = ret.replace("<html><head></head><body>", "").replace("</body></html>", "");
		}
		return ret;
	}
	

	@Override
	public void close() {
		isQuited = true;
		try {
			MitmServer.getInstance().removeHooks(hookIdValue);
			Thread.sleep(1000);
			super.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void quit() {
		isQuited = true;
		try {
			MitmServer.getInstance().removeHooks(hookIdValue);
			sleep(1000);
			super.quit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public byte[] screenshot(WebElement webElement) throws Exception {
		byte[] body = webElement.getScreenshotAs(OutputType.BYTES);
		return body;
	}
	
	public void mouseClick(WebElement webElement) throws Exception {
		Actions actions = new Actions(this);
		actions.moveToElement(webElement).perform();
		sleep(random.nextInt(1500));
		actions.click().perform();
		sleep(random.nextInt(1500));
	}
	
	public void keyboardClear(WebElement webElement, int backSpace) throws Exception {
		mouseClick(webElement);
		for (int k = 0; k < backSpace + random.nextInt(3); k++) {
			webElement.sendKeys(Keys.BACK_SPACE);
			sleep(random.nextInt(150));
		}
	}
	
	public void keyboardInput(WebElement webElement, String text) throws Exception {
		keyboardInput(webElement, text, true);
	}
	
	public void keyboardInput(WebElement webElement, String text, boolean needClick) throws Exception {
		if (needClick)
			mouseClick(webElement);
		int inputed = 0;
		int backTimes = 0;
		int prebackNums = random.nextInt(text.length() / 3);
		for (int k = 0; k < text.length(); k++) {
			webElement.sendKeys(String.valueOf(text.charAt(k)));
			sleep(random.nextInt(500) + 300);
			inputed ++;
			int backNum = inputed >= 3 && backTimes <= prebackNums ?random.nextInt(3) : 0;
			backTimes += backNum;
			for (int i = 0; i < backNum; i++) {
				webElement.sendKeys(Keys.BACK_SPACE);
				sleep(random.nextInt(500) + 300);
				k--;
			}
		}
	}
	
	public void jsInput(WebElement webElement, String text) throws Exception {
		executeScript("arguments[0].value='"+text+"';", webElement);
	}

	@Override
	public void run() {
		while (!isQuited) {
			String url = getCurrentUrl();
			if (url != null && url.startsWith("http")) {
				reInject();
			}
			sleep(1000);
		}
	}
}
