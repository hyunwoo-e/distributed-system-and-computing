package node;

import java.io.*;
import java.net.*;
import java.util.*;
import server.*;
import service.KMP;
import service.Service;

public class NameNode<E> extends Thread {

	private final int proxy_port = 10003;
	private final int name_port = 10004;
	private final int data_port = 10005;
	private final int sock_timeout = 2000;

	private boolean shouldStop;
	public ServerSocket serverSocket;

	private int serviceCount;
	
	private HashMap<Integer, Service> requestServiceMap;
	
	public NameNode() {
		shouldStop = false;
		try {
			serverSocket = new ServerSocket(name_port);
			requestServiceMap = new HashMap<Integer, Service>();
			serviceCount = 0;
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
	
	public void response(int serviceIdentifier) {		
		String responseData = requestServiceMap.get(serviceIdentifier).result.toString().replaceAll(",", "").replaceAll("\\]", "").replaceAll("\\[", "");
		
		try {
			String requestAddress = requestServiceMap.get(serviceIdentifier).requestAddress;
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ServerInfo.getCoordinator(), proxy_port), sock_timeout);
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			
			dos.writeUTF("response");
			dos.writeUTF("");
			dos.writeUTF(requestAddress);
			dos.writeUTF(responseData);
			
			dos.close();
			socket.close();
		} catch (IOException e) {

		}
		
		System.out.println(responseData);
	}
	
	/* DataNode에 작업을 위임 */
	public void delegate_task(String taskProcessorAddress, String requestAddress, int serviceIdentifier, int taskIdentifier, int taskCount, String command, String arg) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(taskProcessorAddress, data_port), sock_timeout);
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			
			dos.writeUTF(requestAddress);
			dos.writeInt(serviceIdentifier);
			dos.writeInt(taskIdentifier);
			dos.writeInt(taskCount);
			dos.writeUTF(command);
			dos.writeUTF(arg);
			
			dos.close();
			socket.close();
		} catch (IOException e) {
			
		}
	}
	
	public void delegate_another_task(int serviceIdentifier) {
		int taskIdentifier;
		
		if(requestServiceMap.get(serviceIdentifier).done.size() < requestServiceMap.get(serviceIdentifier).taskCount) {
			for(int i = 0 ; i < requestServiceMap.get(serviceIdentifier).taskCount ; i++) {
				if(!requestServiceMap.get(serviceIdentifier).done.contains(i)) {
					taskIdentifier = i;
					
					delegate_task(
							ServerInfo.myAddr,
							requestServiceMap.get(serviceIdentifier).requestAddress,
							serviceIdentifier, 
							taskIdentifier, 
							requestServiceMap.get(serviceIdentifier).taskCount, 
							requestServiceMap.get(serviceIdentifier).command, 
							requestServiceMap.get(serviceIdentifier).arg
						);
				}
			}
		}
	}
	
	public void acceptRequest(String requestAddress, String command, String arg) {
		HashMap<String, Integer> nodes = ServerInfo.getAliveServerMap();
		int taskCount = nodes.size();
		int i = 0;
		
		requestServiceMap.put(serviceCount, new Service(requestAddress, taskCount, command, arg));
		if(taskCount > 0) {
			for(Map.Entry<String, Integer> entry : nodes.entrySet()) {
				delegate_task(entry.getKey(), requestAddress, serviceCount, i, taskCount, command, arg);
				i++;
			}
		} else {
			
		}
		serviceCount++;
	}
	
	public void acceptResponse(String responseAddress, int serviceIdentifier, int taskIdentifier, String responseData) {
		if(requestServiceMap.get(serviceIdentifier) == null)
			return;
			
		responseData.replaceAll(",", "").replaceAll("\\]", "").replaceAll("\\]", "");
		StringTokenizer st = new StringTokenizer(responseData, " ");
		while(st.hasMoreTokens()) {
			requestServiceMap.get(serviceIdentifier).result.add(st.nextToken());
		}
			
		requestServiceMap.get(serviceIdentifier).done.add(taskIdentifier);
		
		if(requestServiceMap.get(serviceIdentifier).done.size() == requestServiceMap.get(serviceIdentifier).taskCount) {
			response(serviceIdentifier);
			requestServiceMap.remove(serviceIdentifier);
		} else {
			if(responseAddress.equals(ServerInfo.myAddr)) {
				delegate_another_task(serviceIdentifier);
			}
		}
	}

	public void run() {
		System.out.println("NAMENODE UP");
	
		while(!shouldStop) {
			try {
				Socket socket = serverSocket.accept();
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				
				String type = dis.readUTF();
				
				switch(type) {
					case "request":
						String requestAddress = dis.readUTF();
						String command = dis.readUTF();
						String arg = dis.readUTF();
						acceptRequest(requestAddress, command, arg);
						break;
					case "response":
						String responseAddress = dis.readUTF();
						int serviceIdentifier = dis.readInt();
						int taskIdentifier = dis.readInt();
						String responseData = dis.readUTF();
						acceptResponse(responseAddress, serviceIdentifier, taskIdentifier, responseData);
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
		System.out.println("NAMENODE DOWN");
	}	
}