package node;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import server.*;
import service.*;
import proxy.*;

public class NameNode implements Runnable {
	private boolean shouldStop;
	
	private final int port = 10002;	
	public ServerSocket serverSocket;
	
	public NameNode() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			
		}
	}
	

	public void run() {
		System.out.println("NAMENODE UP");
	
		/* Coordinator는 Up, Slave만 Down 이를 고려해서 DataNode에 작업 분배 및 종합할 수 있는 로직이 필요함. */
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
				
				switch (msg.getType()) {

				}
				
				dis.close();
				socket.close();
			} catch (IOException e) {
				
			}
		}
		
		System.out.println("NAMENODE DOWN");
	}	
}