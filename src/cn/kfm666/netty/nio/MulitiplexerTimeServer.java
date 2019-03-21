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

public class MulitiplexerTimeServer implements Runnable{
	
	private Selector selector;
	
	private ServerSocketChannel server;
	
	private volatile boolean stop;
	
	/**
	 * 初始化多路复用器，绑定端口
	 * @param port
	 */
	public MulitiplexerTimeServer(int port) {
		try {
			selector = Selector.open();
			server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.socket().bind(new InetSocketAddress(port),1024);
			server.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("服务启动在端口："+port);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void stop() {
		this.stop = false;
	}

	@Override
	public void run() {
		// 在新线程中循环从Selector中读取准备就绪的selectionKey
		while (!stop) {
			try {
				selector.select();
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				SelectionKey selectionKey = null;
				Iterator<SelectionKey> iterator = selectionKeys.iterator();
				while (iterator.hasNext()) {
					selectionKey = iterator.next();
					iterator.remove();
					try {
						handInput(selectionKey);
					} catch (Exception e) {
						if(selectionKey != null) {
							selectionKey.cancel();
							if(selectionKey.channel() != null) {
								selectionKey.channel().close();
							}
						}
					}
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				// 线程结束时，将多路复用器关闭，注册到上面的channel会自动关闭，不用重复释放资源
				if(selector != null) {
					try {
						selector.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
		}
		
	}
	
	/**
	 * 接收客户端的消息
	 * @param key
	 * @throws IOException
	 */
	private void handInput(SelectionKey key) throws IOException{
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
					System.out.println("服务器接到命令："+body);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					String currentTime = body.contains("query time order") ?
							format.format(new Date()) : "Unsupported order";
					doWrite(channel, currentTime);
				}else if(readBytes < 0) {
					key.cancel();
					channel.close();
				}else {
					System.err.println("未读到信息");
				}
			}
		}
	}
	
	/**
	 * 向channel中写入数据
	 * @param channel
	 * @param response
	 * @throws IOException
	 */
	private void doWrite(SocketChannel channel,String response) throws IOException{
		if (response != null && response.trim().length()>0) {
			byte[] bytes = response.getBytes("utf-8");
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
			buffer.put(bytes);
			buffer.flip();
			channel.write(buffer);
		}
	}
	
}
