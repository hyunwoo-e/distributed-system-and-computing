package node;

import java.io.*;
import java.net.*;
import java.util.*;
import server.*;
import service.KMP;
import service.Processor;

public class DataNode extends Thread {
	private final int sock_timeout = 2000;
	private final int name_port = 10004;
	private final int data_port = 10005;
	private ServerSocket serverSocket;
	
	private boolean shouldStop;

	private String requestAddress;
	private int serviceIdentifier;
	private int taskIdentifier;
	private int taskCount;
	private String command;
	private String arg;
	
	private Processor processor;
	private String responseData;
	
	public DataNode() {
		shouldStop = false;
		try {
			serverSocket = new ServerSocket(data_port);
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
	
	public void acceptRequest() {
		Socket socket;
		try {
			socket = serverSocket.accept();
			DataInputStream dis = new DataInputStream(socket.getInputStream());

			requestAddress = dis.readUTF();
			serviceIdentifier = dis.readInt();
			taskIdentifier = dis.readInt();
			taskCount = dis.readInt();
			command = dis.readUTF();
			arg = dis.readUTF();
			
			dis.close();
			socket.close();
		} catch (IOException e) {

		}
	}
	
	public void response() {
		try {
			Socket sock = new Socket();
			sock.connect(new InetSocketAddress(ServerInfo.getCoordinator(), name_port), sock_timeout);
			DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
			
			dos.writeUTF("response");
			dos.writeUTF(ServerInfo.myAddr);
			dos.writeInt(serviceIdentifier);
			dos.writeInt(taskIdentifier);
			dos.writeUTF(responseData);				
			
			dos.close();
			sock.close();
		} catch (IOException e) {

		}
	}
	
	public void run() {	
		System.out.println("DATANODE UP");
		
		while(!shouldStop) {
			acceptRequest();
			if(!shouldStop) {
				processor = new KMP(command, arg, taskIdentifier, taskCount);
				responseData = processor.process().toString();
				response();
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {

		}
		System.out.println("DATANODE DOWN");
	}	
}