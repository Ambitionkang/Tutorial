package cn.kfm666.netty.bio;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.omg.CORBA.PolicyError;

public class TimeServer {

	public static void main(String[] args) {
		int port = 8080;
		if(args != null && args.length>0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				port = 8080;
			}
		}
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("系统启动在端口"+port);
			Socket socket = null;
			while (true) {
				socket = server.accept();
				new Thread(new TimeHandler(socket)).start();				
			}
			
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally {
			if(server != null) {
				try {
					server.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				server = null;
			}
		}
		
	}

}
