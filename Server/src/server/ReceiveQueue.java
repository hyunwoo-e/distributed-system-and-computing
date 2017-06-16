package server;

import java.io.*;
import java.net.*;

public class ReceiveQueue implements Runnable {
	public final int port = 10001;	
	public ServerSocket serverSocket;
	
	public ReceiveQueue() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		Thread.currentThread();
		while(!Thread.interrupted()) {			
			try {
				Socket socket = serverSocket.accept();
				DataInputStream dis = new DataInputStream(socket.getInputStream());

				String type = dis.readUTF();
				String flag = dis.readUTF();
				String addr = dis.readUTF(); addr = socket.getInetAddress().toString().replaceAll("/", "");
				String data = dis.readUTF();
				
				Message msg = new Message(type, flag, addr, data);
				
				PassiveQueue<Message> manager = Server.getManager(msg.getType());
				manager.accept(msg);
				
				dis.close();
				socket.close();
			} catch (IOException e) {
				
			}
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
