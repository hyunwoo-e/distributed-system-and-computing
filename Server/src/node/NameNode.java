package node;

import java.io.*;
import java.net.*;
import java.util.*;
import server.*;

public class NameNode<E> implements Runnable {

	private final int name_port = 10002;
	
	public ServerSocket serverSocket;
	private Socket socket;
	private DataInputStream dis;
	
	HashMap<String, Service> requestMap; //client, text, pattern
	
	public NameNode() {
		try {
			serverSocket = new ServerSocket(name_port);
			requestMap = new HashMap<String, Service>();
		} catch (IOException e) {
			
		}
	}
	
	public void request() {
		try {
			String addr = dis.readUTF();
			String text = dis.readUTF();
			String pattern = dis.readUTF();
			
			requestMap.put(addr, new Service(addr, new KMP(text, pattern)));
			if(requestMap.get(addr).taskCount > 0)
				requestMap.get(addr).devide();
			
			
			dis.close();
			socket.close();
		} catch (IOException e) {

		}
	}
	
	public void merge() {
		try {
			String addr = dis.readUTF();
			int cur = dis.readInt();
			
			while(dis.available() > 0)
			{
				int index = dis.readInt();
				int realIndex = cur+index;

				if(index == -1)
				{
					requestMap.get(addr).done.add(cur);
					if(requestMap.get(addr).done.size() == requestMap.get(addr).taskCount)
					{
						for(Integer i : requestMap.get(addr).result) {
							System.out.print(i + " ");
						}
						System.out.println("");
						requestMap.remove(addr);
					}
				}
				else {
					requestMap.get(addr).result.add(realIndex);
				}
			}
		} catch (IOException e) {

		}
	}

	public void run() {
		System.out.println("NAMENODE UP");
	
		Thread.currentThread();
		while(!Thread.interrupted()) {
			try {
				socket = serverSocket.accept();
				dis = new DataInputStream(socket.getInputStream());
				
				//type, <type specific>
				String type = dis.readUTF();
				
				switch(type) {
					case "REQUEST":
						request();
						break;
					case "MERGE":
						merge();
						break;
				}
			} catch (IOException e) {
				
			}
		}
		
		System.out.println("NAMENODE DOWN");
	}	
}