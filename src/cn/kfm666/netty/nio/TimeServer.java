package cn.kfm666.netty.nio;

public class TimeServer {
	public static void main(String args[]) {
		int port = 8080;
		if(args != null && args.length>0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				port = 8080;
			}
		}
		MulitiplexerTimeServer server = new MulitiplexerTimeServer(port);
		new Thread(server).start();
	}
}
