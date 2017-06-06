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
					/* ���Ŀ�� Coordinator�� Down���� �˸� */
					/* Ŭ���̾�Ʈ ���Ͻô� ���� ���Ͻÿ� �ٽ� ���񽺸� ��û */
					
					/* Coordinator�� �����ϱ� ���� �Ϸ����� ���� */
					if(Server.getIsCoordinatorAlive() == true && Server.getIsElectionStarted() == false) {
						Server.setIsCoordinatorAlive(false);
					}
				}
			}
		}
	}
}
