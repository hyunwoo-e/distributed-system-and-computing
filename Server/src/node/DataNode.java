package node;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import server.*;
import service.*;

public class DataNode implements Runnable {
	private final int port = 10003;	
	public ServerSocket serverSocket;
	
	public DataNode() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			
		}
	}
	
	public void run() {	
		System.out.println("DATANODE UP");
		
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
		
		System.out.println("DATANODE DOWN");
	}	
}