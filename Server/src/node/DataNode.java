package node;

import java.io.*;
import java.net.*;
import java.util.*;
import server.*;

public class DataNode implements Runnable {
	private final int sock_timeout = 2000;
	
	private final int name_port = 10002;
	private final int data_port = 10003;	
	
	public ServerSocket serverSocket;
	
	public DataNode() {
		try {
			serverSocket = new ServerSocket(data_port);
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

				String addr = dis.readUTF();
				int cur = dis.readInt();
				String text = dis.readUTF();
				String pattern = dis.readUTF();

				KMP kmp = new KMP(text, pattern);
				kmp.make_pi();
				
				ArrayList<Integer> indexList;
				indexList = kmp.find_index();
				
				Socket sock = new Socket();
				sock.connect(new InetSocketAddress(Server.getCoordinator(), name_port), sock_timeout);
				
				DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
				dos.writeUTF("MERGE");
				dos.writeUTF(addr);
				dos.writeInt(cur);
								
				for(Integer i : indexList) {
					dos.writeInt(i);
				}
				dos.writeInt(-1);
				
				dos.close();
				sock.close();
				
				dis.close();
				socket.close();
			} catch (IOException e) {
				
			}
		}
		
		System.out.println("DATANODE DOWN");
	}	
}