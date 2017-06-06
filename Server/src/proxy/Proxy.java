package proxy;

import java.io.*;
import java.net.*;
import java.util.*;
import server.*;

public class Proxy implements Runnable {
	
	private final int port = 10000;	
	public ServerSocket serverSocket;
	
	public Proxy() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			
		}
	}
	
	public void run() {	
		System.out.println("SERVERPROXY UP");
		
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
		
		System.out.println("SERVERPROXY DOWN");
	}	
}