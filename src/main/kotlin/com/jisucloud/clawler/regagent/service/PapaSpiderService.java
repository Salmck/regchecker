package com.jisucloud.clawler.regagent.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.jisucloud.clawler.regagent.http.OKHttpUtil;
import com.jisucloud.clawler.regagent.util.CountableFiberPool;
import com.jisucloud.clawler.regagent.util.TimerRecoder;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Service
public class PapaSpiderService extends Thread {
	
	public static final String PAPATASK_QUEUE_KEY = "papatask_queue_key";

	@Autowired
	private StringRedisTemplate redisTemplate;
	
	private OkHttpClient okHttpClient = OKHttpUtil.createOkHttpClient();
	
	private CountableFiberPool fiberPool = new CountableFiberPool(20000);
	
	@PostConstruct
	private void init() {
		start();
	}
	
	public void addPapaTask(PapaTask papaTask) {
		if (papaTask != null) {
			log.warn("添加任务:"+papaTask);
			redisTemplate.opsForList().rightPush(PAPATASK_QUEUE_KEY, JSON.toJSONString(papaTask));
		}
	}
	
	private PapaTask takePapaTask() {
		while(true) {
			String str = redisTemplate.opsForList().leftPop(PAPATASK_QUEUE_KEY, 10, TimeUnit.SECONDS);
			if (str != null) {
				return JSON.parseObject(str, PapaTask.class);
			}
		}
	}
	
	@Override
	public void run() {
		PapaTask papaTask = null;
		while (true) {
			papaTask = takePapaTask();
			fiberPool.waitIdleThread();
			fiberPool.execute(new PapaSpiderTaskRunnable(papaTask));
		}
	}
	
	public final class PapaSpiderTaskRunnable implements Runnable {
		
		private AtomicInteger successCount = new AtomicInteger();
		private AtomicInteger failureCount = new AtomicInteger();
		private int fiberSize = 0;
		private PapaTask papaTask = null;
		
		public PapaSpiderTaskRunnable(PapaTask papaTask) {
			this.papaTask = papaTask;
		}

		@Override
		public void run() {
			log.info("开始任务("+papaTask.getId()+")");
			TimerRecoder timerRecoder = new TimerRecoder().start();
			for (Class<? extends PapaSpider> clz : TestValidPapaSpiderService.TEST_SUCCESS_PAPASPIDERS) {
				fiberPool.waitIdleThread();
				fiberSize ++;
				fiberPool.execute(new PapaSpiderRunnable(papaTask, clz, successCount, failureCount));
			}
			waitCheckFinished();
			String useTime = timerRecoder.getText();
			log.info("任务("+papaTask.getId()+")结束。用时"+useTime+",成功撞库平台"+successCount+"个,失败"+failureCount+"个。");
		}
		
		private void waitCheckFinished() {
			while ((successCount.get() + failureCount.get()) < fiberSize) {
				try {
					Strand.sleep(1000);
				} catch (SuspendExecution e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * 具体撞库任务
	 * @author stephen
	 *
	 */
	public final class PapaSpiderRunnable implements Runnable {
		
		private PapaTask papaTask = null;
		
		private Class<? extends PapaSpider> papaSpiderClz;
		
		private AtomicInteger successCount = null;
		private AtomicInteger failureCount = null;
		
		
		public PapaSpiderRunnable(PapaTask papaTask, Class<? extends PapaSpider> papaSpiderClz,
				AtomicInteger successCount, AtomicInteger failureCount) {
			this.papaTask = papaTask;
			this.papaSpiderClz = papaSpiderClz;
			this.successCount = successCount;
			this.failureCount = failureCount;
		}

		@Override
		public void run() {
			if (papaTask.getTelephone() != null) {
				try {
					PapaSpider instance = null;
					instance = papaSpiderClz.newInstance();
					if (papaTask.isNeedlessCheck(instance.platform())) {
						log.info("任务(" + papaTask.getId() + "),不需要撞库平台"+instance.platform());
						return;
					}
					notifyTelephone(instance, instance.checkTelephone(papaTask.getTelephone()));
					successCount.incrementAndGet();
				} catch (Exception e) {
					failureCount.incrementAndGet();
					log.warn("任务("+papaTask.getId()+")撞库"+papaSpiderClz.getName()+"失败", e);
				}
				finished();
			}
		}
		
		private void finished() {
			Map<String,Object> result = new HashMap<>();
			result.put("method", "finished");
			postResult(result);
		}
		
		private void notifyTelephone(PapaSpider instance, boolean registed) {
			Map<String,Object> result = new HashMap<>();
			result.put("method", "notify");
			Map<String, String> fields = instance.getFields();
			if (fields == null) {
				fields = new HashMap<>();
			}
			Account data = Account.builder()
				.username(papaTask.getTelephone())
				.registed(registed)
				.platform(instance.platform())
				.platformName(instance.platformName())
				.platformMsg(instance.message())
				.addTag(instance.tags())
				.fields(fields).build();
			result.put("data", data);
			postResult(result);
		}

		private void postResult(Map<String, Object> result) {
			result.put("id", papaTask.getId());
			String url = papaTask.getCallurl();
			RequestBody requestBody = FormBody.create(MediaType.parse("application/json;charset=utf-8"), JSON.toJSONString(result));
			Request request = new Request.Builder().url(url)
					.post(requestBody)
					.build();
			Response response = null;
			for (int i = 0; i < 3; i++) {
				try {
					response = okHttpClient.newCall(request).execute();
					if (response.code() != 200) {
						log.warn("推送结果失败:"+papaTask.getId() + "status:" + response.code());
						continue;
					}
				} catch (Exception e) {
					log.warn("推送结果异常:"+papaTask.getId(), e);
					continue;
				}
				break;
			}
		}
	}
	
}
