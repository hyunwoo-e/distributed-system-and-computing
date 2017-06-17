package server;

import java.io.*;
import java.net.*;

public class SendQueue extends PassiveQueue<Message> implements Runnable {
	private final int port;
	private final int sock_timeout = 1000;
	private boolean shouldStop;
	
	public SendQueue(int port) {
		this.port = port;
		shouldStop = false;
	}
	
	public void stop() {
		shouldStop = true;
		destroy();
	}
	
	public void run() {
		Socket socket;
		DataOutputStream dos;
		Message msg;
		
		while(!shouldStop) {
			msg = super.release();

			if(msg != null) {
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress(msg.getAddr(), port), sock_timeout);
					
					dos = new DataOutputStream(socket.getOutputStream());
					dos.writeUTF(msg.getType());
					dos.writeUTF(msg.getFlag());
					dos.writeUTF(msg.getAddr());
					dos.writeUTF(msg.getData());
					
					dos.close();
					socket.close();
				} catch (IOException e) {
					if(msg.getAddr().equals(Server.getCoordinator())) {
						/* 브로커에 Coordinator가 Down됨을 알림 */
						/* 클라이언트 프록시는 서버 프록시에 다시 서비스를 요청 */
						
						/* Coordinator를 선정하기 위해 일렉션을 실행 */
						if(Server.getIsCoordinatorAlive() == true) {
							Server.setIsCoordinatorAlive(false);
						}
					}
				}
			}
		}
	}
}
