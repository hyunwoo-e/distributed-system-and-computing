package server;

import java.io.*;
import java.net.*;

public class ReceiveQueue implements Runnable {
	
	private PassiveQueue<Message> controller;
	private boolean shouldStop;
	
	private int port;	
	public ServerSocket serverSocket;
	
	public ReceiveQueue(PassiveQueue<Message> controller, int port) {
		this.controller = controller;
		this.port = port;
		shouldStop = false;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			
		}
	}
	
	public void stop() {
		try {
			shouldStop = true;
			serverSocket.close();
		} catch (IOException e) {

		}		
	}
	
	public void run() {
		while(!shouldStop) {			
			try {
				Socket socket = serverSocket.accept();
				DataInputStream dis = new DataInputStream(socket.getInputStream());

				String type = dis.readUTF();
				String flag = dis.readUTF();
				String addr = dis.readUTF(); addr = socket.getInetAddress().toString().replaceAll("/", "");
				String data = dis.readUTF();
				
				Message msg = new Message(type, flag, addr, data);
				
				controller.accept(msg);
				
				dis.close();
				socket.close();
			} catch (IOException e) {
				
			}
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			
		}
	}
}
