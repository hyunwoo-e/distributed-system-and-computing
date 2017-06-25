package proxy;

import java.io.*;
import java.net.*;
import java.util.*;
import server.*;

public class Proxy extends Thread implements ServerProxy {
	private final int broker_port = 10000;
	private final int server_port = 10003;	
	private final int client_port = 10006;	
	private final int name_port = 10004;
	private int sock_timeout = 2000;
	
	private final int maxCacheCount = 10;
	private HashMap<String, String> cacheMap;
	
	private boolean shouldStop;
	
	public ServerSocket serverSocket;
	private DataInputStream dis;
	
	public Proxy() {
		shouldStop = false;
		cacheMap = new HashMap<String, String>();
		try {
			serverSocket = new ServerSocket(server_port);
		} catch (IOException e) {
			
		}
	}
	
	public void _stop() {
		shouldStop = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			
		}
	}
	
	public void register_service() {
		Socket socket;
		DataOutputStream dos;

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(borker_address, broker_port), sock_timeout);
			
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeUTF("register_service");
			dos.writeUTF("");
			dos.writeUTF("");
			dos.writeUTF("");
			
			dos.close();
			socket.close();
		} catch (IOException e) {
			
		}
	}
	
	public void remove_service(String addr) {
		Socket socket;
		DataOutputStream dos;

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(borker_address, broker_port), sock_timeout);
			
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeUTF("remove_service");
			dos.writeUTF("");
			dos.writeUTF("");
			dos.writeUTF(addr);
			
			dos.close();
			socket.close();
		} catch (IOException e) {
			
		}
	}
	
	public void acceptRequest(Message msg) {
		Socket socket;
		DataOutputStream dos;

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(ServerInfo.myAddr, name_port), sock_timeout);
			
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeUTF(msg.getType());
			dos.writeUTF(msg.getAddr());
			dos.writeUTF(msg.getFlag());
			dos.writeUTF(msg.getData());
			
			dos.close();
			socket.close();
		} catch (IOException e) {
			
		}
		
		if(cacheMap.size() > maxCacheCount) {
			Iterator<String> iteratorKey = cacheMap.keySet().iterator();
			cacheMap.remove(iteratorKey.next());
		}
		cacheMap.put(msg.getFlag()+msg.getData(), null);
	}
	
	public void acceptResponse(Message msg) {
		Socket socket;
		DataOutputStream dos;

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(msg.getAddr(), client_port), sock_timeout);
			dos = new DataOutputStream(socket.getOutputStream());
						
			dos.writeUTF("response");
			dos.writeUTF("");
			dos.writeUTF("");
			dos.writeUTF(msg.getData());
			
			dos.close();
			socket.close();
		} catch (IOException e) {
			
		}
		
		if(cacheMap.containsKey(msg.getFlag())) {
			if(cacheMap.get(msg.getFlag()) == null)
				cacheMap.put(msg.getFlag(), msg.getData());
		}
	}
	
	public void run() {	
		System.out.println("SERVERPROXY UP");
		
		for(int i = 0; i < ServerInfo.totalServerList.size() ; i++)
			remove_service(ServerInfo.totalServerList.get(i));

		register_service();
		
		while(!shouldStop) {			
			try {
				Socket socket = serverSocket.accept();
				dis = new DataInputStream(socket.getInputStream());

				String type = dis.readUTF();
				String flag = dis.readUTF();
				String addr = dis.readUTF();
				String data = dis.readUTF();
				
				Message msg = new Message(type, flag, addr, data);
								
				switch (msg.getType()) {
					case "register_service":
						System.out.println("REGISTERED IN BROKER");
						break;
					case "request":
						if(cacheMap.containsKey(msg.getFlag()+msg.getData()) && (cacheMap.get(msg.getFlag()+msg.getData()) != null)) {
							acceptResponse(new Message("", "", msg.getAddr(), cacheMap.get(msg.getFlag()+msg.getData())));
						} else {
							acceptRequest(msg);
						}
						break;
					case "response":
						acceptResponse(msg);
						break;
				}
				
				dis.close();
				socket.close();
			} catch (IOException e) {
				
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {

		}
		System.out.println("SERVERPROXY DOWN");
	}	
}