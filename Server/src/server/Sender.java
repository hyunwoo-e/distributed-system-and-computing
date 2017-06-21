package server;

import java.io.*;
import java.net.*;

public class Sender extends PassiveQueue<Message> implements Runnable {
	private final int port;
	private final int sock_timeout = 1000;
	private boolean shouldStop;
	
	public Sender(int port) {
		this.port = port;
		shouldStop = false;
	}
	
	public void stop() {
		shouldStop = true;
		destroy();
	}
	
	public void run() {
		while(!shouldStop) {
			send();
		}
	}
	
	public void errorHandling(String addr) {
		if(addr.equals(ServerInfo.getCoordinator())) {
			/* ���Ŀ�� Coordinator�� Down���� �˸� */
			/* Ŭ���̾�Ʈ ���Ͻô� ���� ���Ͻÿ� �ٽ� ���񽺸� ��û */
			
			/* Coordinator�� �����ϱ� ���� �Ϸ����� ���� */
			if(ServerInfo.getIsCoordinatorAlive() == true) {
				ServerInfo.setIsCoordinatorAlive(false);
				Server.alertMessage.enqueue("START_ELECTION");
			}
		}
	}
	
	public void send() {
		Socket socket;
		DataOutputStream dos;
		Message msg;
		
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
				errorHandling(msg.getAddr());
			}
		}
	}
}
