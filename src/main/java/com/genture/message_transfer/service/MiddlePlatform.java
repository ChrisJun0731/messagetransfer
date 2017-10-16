package com.genture.message_transfer.service;

import com.genture.message_transfer.threads.SenderCallable;
import com.genture.message_transfer.util.Receiver;
import com.genture.message_transfer.util.Sender;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MiddlePlatform {

	private InputStream is;
	private OutputStream os;

	private Receiver receiver = new Receiver(is);
	private Sender sender = new Sender(os);

	public MiddlePlatform(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
	}

	/**
	 * 启动中转平台
	 */
	public void work() {
		List<String> allMessage = new ArrayList();
		List<String> messages = splitMessage(receiveMessage());
		ExecutorService pool = Executors.newCachedThreadPool();
		for (int i = 0; i < messages.size(); i++) {
			Future future = pool.submit(new SenderCallable(messages.get(i)));
			try {
				String json = (String) future.get();
				allMessage.add(json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String reply = combineMessage(allMessage);
		sender.send(sender.combine(reply.getBytes()));
		byte[] frame = receiver.receiveFrame();

		int exitFlag1 = 0;
		while (!receiver.isValid(frame)) {
			responseFrameValid();
			exitFlag1++;
			if (exitFlag1 > 10) {
				break;
			}
		}

		int exitFlag2 = 0;
		while (receiver.parseAndUnescapeFrameBody(frame)[0] == 0x00) {
			sender.send(sender.combine(reply.getBytes()));
			exitFlag2++;
			if (exitFlag2 > 10) {
				break;
			}
		}
	}

	/**
	 * 接受字符串消息
	 *
	 * @return 消息字符串
	 */
	public String receiveMessage() {
		String json = "";
		byte[] frame = receiver.receiveFrame();
		while (!receiver.isValid(frame)) {
			responseFrameInvalid();
		}
		responseFrameValid();
		byte[] unescapeFrameBody = receiver.parseAndUnescapeFrameBody(frame);
		json = receiver.conventFrameBodyToString(unescapeFrameBody);
		return json;
	}

	/**
	 * 拆分消息
	 *
	 * @param config_json 接受到的消息
	 * @return 拆分后的消息
	 */
	private List<String> splitMessage(String config_json) {
		List<String> configs = new ArrayList();
		JSONObject configObj = JSONObject.fromObject(config_json);
		JSONArray commands = (JSONArray) configObj.get("commands");
		JSONArray resources = (JSONArray) configObj.get("resources");
		for (int i = 0; i < commands.size(); i++) {
			JSONObject ipcJson = (JSONObject) commands.get(i);
			String config = "{commands:[" + ipcJson.toString() + "], resources:" + resources.toString() + "}";
			configs.add(config);
		}
		return configs;
	}

	/**
	 * 响应接受到的数据帧无效
	 */
	public void responseFrameInvalid() {
		try {
			this.os.write(sender.combine(new byte[]{0x00}));
			this.os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 响应数据帧有效
	 */
	public void responseFrameValid() {
		try {
			this.os.write(sender.combine(new byte[]{0x01}));
			this.os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将多个message组合为一个message
	 *
	 * @param messages
	 * @return
	 */
	public String combineMessage(List<String> messages) {
		String output = "{\"commands\":[";
		String resources = "";
		for (int i = 0; i < messages.size(); i++) {
			JSONObject config = JSONObject.fromObject(messages.get(i));
			String command = ((JSONObject) ((JSONArray) config.get("commands")).get(0)).toString();
			resources = ((JSONArray) config.get("resources")).toString();
			output += command;
		}
		output += "],resources:" + resources;
		return output;
	}

}
