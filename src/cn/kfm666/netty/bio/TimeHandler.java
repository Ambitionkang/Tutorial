package cn.kfm666.netty.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class TimeHandler implements Runnable{

	private Socket socket;
	
	public TimeHandler(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		BufferedReader reader = null;
		PrintWriter writer = null;
		try {
			reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			writer = new PrintWriter(this.socket.getOutputStream(),true);
			String currentTime = null;
			StringBuilder body = new StringBuilder();
//			while(true) {
//				String msg = reader.readLine();
//				if(msg == null) {
//					break;
//				}
//				body.append(msg);
//				
//			}
//			System.out.println("接到客户端消息："+body.toString());
			writer.println("收到消息："+new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					reader = null;
				}
			}
			if(writer != null) {
				writer.close();
				writer = null;
			}
			if(this.socket != null) {
				try {
					this.socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					this.socket = null;
				}
			}
		}
		
	}

	

}
