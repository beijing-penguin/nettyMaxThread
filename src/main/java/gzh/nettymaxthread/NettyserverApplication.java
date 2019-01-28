package gzh.nettymaxthread;

/**
 * 服务器端启动类
 * @author gongzhihao
 *
 */
public class NettyserverApplication {
	//监听端口范围8000~8100
	private static final int BEGIN_PORT = 8000; 
	private static final int N_PORT = 100;

	public static void main(String[] args) {

		new Server().start(BEGIN_PORT, N_PORT);
	}
}
