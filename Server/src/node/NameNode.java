package node;

import java.io.*;
import java.net.*;
import java.util.*;
import server.*;

public class NameNode<E> implements Runnable {

	private final int port = 10002;
	private final int sock_timeout = 2000;
	private int id;
	
	public ServerSocket serverSocket;
	private Socket socket;
	private DataInputStream dis;
	
	HashMap<Integer, Service> requestMap;
	
	public NameNode() {
		try {
			serverSocket = new ServerSocket(port);
			requestMap = new HashMap<Integer, Service>();
			id = 0;
		} catch (IOException e) {
			
		}
	}
	
	public void respond_to_client(int id) {		
		try {
			String addr = requestMap.get(id).addr;
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(addr, port), sock_timeout);

			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			for(Integer i : requestMap.get(id).result) {
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
			id++;
			
			requestMap.put(id, new Service(id, addr, new KMP(text, pattern)));
			if(Server.getAliveServerMap().size() > 0)
				requestMap.get(id).devide(Server.getAliveServerMap().size());
		} catch (IOException e) {

		}
	}
	
	public void merge() {
		try {
			int id = dis.readInt();
			String addr = dis.readUTF();
			int cur = dis.readInt();
			
			if(requestMap.get(id) == null)
				return;
						
			while(dis.available() > 0)
			{
				int index = dis.readInt();
				int realIndex = cur+index;

				if(index == -1)
				{
					boolean isAllDone = true;
					requestMap.get(id).done.put(cur, true);
					for(Map.Entry<Integer, Boolean> entry : requestMap.get(id).done.entrySet()) {
						if(!entry.getValue()) {
							isAllDone = false;
							break;
						}
					}
					
					if(isAllDone)
					{
						respond_to_client(id);
						requestMap.remove(id);
					} else {
						//addr cur text pattern
						for(Map.Entry<Integer, KMP> entry : requestMap.get(id).kmpMap.entrySet()) {
							if(!requestMap.get(id).done.get(entry.getKey())){
								requestMap.get(id).allocate(entry.getKey(), socket.getInetAddress().toString().replaceAll("/", ""));
								break;
							}
						}
					}
				}
				else {
					requestMap.get(id).result.add(realIndex);
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