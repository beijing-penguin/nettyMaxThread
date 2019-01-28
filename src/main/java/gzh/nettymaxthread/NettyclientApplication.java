package gzh.nettymaxthread;

/**
 * 客户端启动类
 * @author gongzhihao
 *
 */
public class NettyclientApplication {
	//服务器端端口范围8000~8100
	private static final int BEGIN_PORT = 8000;
	private static final int N_PORT = 100;

	public static void main(String[] args) {

		new Client().start(BEGIN_PORT, N_PORT);
	}
}
