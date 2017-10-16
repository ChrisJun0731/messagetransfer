package com.genture.message_transfer.threads;

import com.genture.message_transfer.util.Receiver;
import com.genture.message_transfer.util.Sender;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2017/10/16.
 */
public class SenderCallable implements Callable {

	private final int defaultPort = 5005;
	private String config;

	public SenderCallable(String config){
		this.config = config;
	}

	public String call(){
		String json = "";
		JSONObject configObj = JSONObject.fromObject(this.config);
		JSONArray commands = (JSONArray)configObj.get("commands");
		JSONObject ipc = (JSONObject)commands.get(0);
		String ip = (String) ipc.get("ip");
		try {
			Socket socket = new Socket(ip, defaultPort);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			Sender sender = new Sender(os);
			Receiver receiver = new Receiver(is);
			sender.send(sender.combine(sender.escape(config.getBytes())));
			byte[] frame = receiver.receiveFrame();
			while(!receiver.isValid(frame)){
				sender.send(sender.combine(new byte[]{0x00}));
			}
			json = receiver.conventFrameBodyToString(receiver.parseAndUnescapeFrameBody(frame));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
}
