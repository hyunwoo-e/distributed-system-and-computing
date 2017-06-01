package server;

import java.io.*;
import java.net.*;
import queue.*;

public class Sender extends Thread implements MessageDefination {
	private final int port = 10001;
	private Socket socket;
	private DataOutputStream dos;
	
	public Sender() {
		Thread.currentThread();
	}

	public void run() {
		Message msg = null;
		
		while(!Thread.interrupted()) {
			try {	
				 msg = MessageQueue.dequeue();
				if (msg != null) {
					socket = new Socket();
					socket.connect(new InetSocketAddress(msg.getReceiver(), port), SOCKET_TIMEOUT);
					
					dos = new DataOutputStream(socket.getOutputStream());
					dos.writeUTF(msg.getSender());
					dos.writeUTF(msg.getReceiver());
					dos.writeInt(msg.getManager());
					dos.writeInt(msg.getType());
					
					dos.close();
					socket.close();
				}
			} catch (IOException e) {
				if(msg.getReceiver() == Common.coordinator) {
					if(Common.isAlive == true) {
						/* 브로커에 Coordinator가 Down됨을 알림 */
						/* 클라이언트 프록시는 서버 프록시에 다시 서비스를 요청 */
						
						/* Coordinator를 선정하기 위해 일렉션을 실행 */
						Common.isAlive = false;
						Common.em.send_election();
					}
				}
			}
		}
		
		System.out.println("송신 쓰레드 정상 종료");
	}
}
