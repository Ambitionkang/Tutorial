package cn.kfm666.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class TimeClient implements Runnable{

	public static void main(String[] args) {
		TimeClient client = new TimeClient("127.0.0.1", 8080);
		new Thread(client).start();

	}
	
	private String host;
	int port;
	private Selector selector;
	private SocketChannel channel;
	private volatile boolean stop;
	
	public TimeClient(String host,int port) {
		this.host = host;
		this.port = port;
		
		try {
			selector = Selector.open();
			channel = SocketChannel.open();
			channel.configureBlocking(false);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	

	@Override
	public void run() {
		try {
			doConnect();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		while (!stop) {
			try {
				selector.select(1000);
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectionKeys.iterator();
				SelectionKey key = null;
				while (iterator.hasNext()) {
					key = iterator.next();
					iterator.remove();
					try {
						if(key.isValid()) {
							if(key.isAcceptable()) {
								ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
								SocketChannel channel = ssc.accept();
								ByteBuffer readBuffer = ByteBuffer.allocate(1024);
								int readBytes = channel.read(readBuffer);
								if(readBytes>0) {
									readBuffer.flip();
									byte[] bytes = new byte[readBuffer.remaining()];
									readBuffer.get(bytes);
									String body = new String(bytes, "utf-8");
									System.out.println("服务器时间为："+body);
									this.stop = true;
								}else if(readBytes < 0) {
									key.cancel();
									channel.close();
								}else {
									System.err.println("未读到信息");
								}
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
					
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
	}
	
	public void handleInput(SelectionKey key) {
		
	}
	
	private void doConnect() throws IOException{
		if(channel.connect(new InetSocketAddress(host, port))) {
			channel.register(selector, SelectionKey.OP_READ);
			doWrite(channel);
		}else {
			channel.register(selector, SelectionKey.OP_CONNECT);
		}
	}
	
	private void doWrite(SocketChannel channel) throws IOException{
		byte[] bytes = "query time order".getBytes("utf-8");
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		channel.write(buffer);
		if(!buffer.hasRemaining()) {
			System.out.println("发送命令到服务器成功");
		}
	}
	
}
