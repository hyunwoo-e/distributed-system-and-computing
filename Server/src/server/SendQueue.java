package server;

import java.io.*;
import java.net.*;

public class SendQueue extends PassiveQueue<Message> implements Runnable {
	private final int port = 10001;
	private final int sock_timeout = 1000;
	private boolean shouldStop;
	
	public SendQueue() {
		shouldStop = false;
	}
	
	public void stop() {
		shouldStop = true;
	}
	
	public void run() {
		Socket socket;
		DataOutputStream dos;
		Message msg;
		
		while(!shouldStop) {
			msg = super.release();
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
					/* ���Ŀ�� Coordinator�� Down���� �˸� */
					/* Ŭ���̾�Ʈ ���Ͻô� ���� ���Ͻÿ� �ٽ� ���񽺸� ��û */
					
					/* Coordinator�� �����ϱ� ���� �Ϸ����� ���� */
					if(Server.getIsCoordinatorAlive() == true) {
						Server.setIsCoordinatorAlive(false);
					}
				}
			}
		}
	}
}
