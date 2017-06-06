package server;

import java.io.*;
import java.net.*;

public class MessageQueue extends PassiveQueue<Message> implements Runnable {
	private final int SOCKET_TIMEOUT = 1000;
	private int port = 10001;
	
	public MessageQueue(int port) {
		this.port = port;
	}
	
	public synchronized void acceptMessage(Message msg) {
		super.accept(msg);
	}
	
	public void run() {
		Socket socket;
		DataOutputStream dos;
		Message msg;
		
		for(;;){
			msg = super.release();
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress(msg.getAddr(), port), SOCKET_TIMEOUT);
				
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
					if(Server.getIsCoordinatorAlive() == true && Server.getIsElectionStarted() == false) {
						Server.setIsCoordinatorAlive(false);
					}
				}
			}
		}
	}
}
