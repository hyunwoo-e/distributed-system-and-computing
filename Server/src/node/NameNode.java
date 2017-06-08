package node;

import java.io.*;
import java.net.*;
import java.util.*;
import server.*;

public class NameNode<E> implements Runnable {

	private final int port = 10002;
	private final int sock_timeout = 2000;
	
	public ServerSocket serverSocket;
	private Socket socket;
	private DataInputStream dis;
	
	HashMap<String, Service> requestMap;
	
	public NameNode() {
		try {
			serverSocket = new ServerSocket(port);
			requestMap = new HashMap<String, Service>();
		} catch (IOException e) {
			
		}
	}
	
	public void respond_to_client(String addr) {		
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(addr, port), sock_timeout);

			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			for(Integer i : requestMap.get(addr).result) {
				dos.writeInt(i);
			}
			dos.close();
			socket.close();
		} catch (IOException e) {

		}
	}
	
	public void request() {
		try {
			String addr = dis.readUTF();
			String text = dis.readUTF();
			String pattern = dis.readUTF();
			
			requestMap.put(addr, new Service(addr, new KMP(text, pattern)));
			if(Server.getAliveServerMap().size() > 0)
				requestMap.get(addr).devide(Server.getAliveServerMap().size());
		} catch (IOException e) {

		}
	}
	
	public void merge() {
		try {
			String addr = dis.readUTF();
			int cur = dis.readInt();
			
			if(requestMap.get(addr) == null)
				return;
						
			while(dis.available() > 0)
			{
				int index = dis.readInt();
				int realIndex = cur+index;

				if(index == -1)
				{
					boolean isAllDone = true;
					requestMap.get(addr).done.put(cur, true);
					for(Map.Entry<Integer, Boolean> entry : requestMap.get(addr).done.entrySet()) {
						if(!entry.getValue()) {
							isAllDone = false;
							break;
						}
					}
					
					if(isAllDone)
					{
						respond_to_client(addr);
						requestMap.remove(addr);
					} else {
						//addr cur text pattern
						for(Map.Entry<Integer, KMP> entry : requestMap.get(addr).kmpMap.entrySet()) {
							if(!requestMap.get(addr).done.get(entry.getKey())){
								requestMap.get(addr).allocate(entry.getKey(), socket.getInetAddress().toString().replaceAll("/", ""));
								break;
							}
						}
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
				
				dis.close();
				socket.close();
			} catch (IOException e) {
				
			}
		}
		
		System.out.println("NAMENODE DOWN");
	}	
}