package server;

import java.io.*;
import java.net.*;
import queue.*;

public class Receiver extends Thread implements MessageDefination {	
	private final int port = 10001;	
	private ServerSocket serverSocket;
	private Socket socket;
	private DataInputStream dis;
	
	public Receiver() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Thread.currentThread();
	}
	
	public void run() {
		while(!Thread.interrupted()) {
			try {
				socket = serverSocket.accept();
				
				dis = new DataInputStream(socket.getInputStream());
				String sender = dis.readUTF();
				String receiver = dis.readUTF();
				int manager = dis.readInt();
				int type = dis.readInt();
				
				switch (manager) {
					case TO_ELECTION_MANAGER :
						ElectionMessageQueue.enqueue(new Message(sender, receiver, manager, type));
						break;
					case TO_RESOURCE_MANAGER :
						HeartBeatingMessageQueue.enqueue(new Message(sender, receiver, manager, type));
						break;
				}
				
				dis.close();
				socket.close();
			} catch (IOException e) {
				
			}
		}
		
		try {
			serverSocket.close();
			System.out.println("수신 쓰레드 정상 종료");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}