package broker;

import java.io.*;
import java.net.*;
import java.util.*;

public class Broker implements Runnable {
	private ArrayList<String> serverProxyList = new ArrayList<String>();
	private int serverProxyIndex;
	
	private final int broker_port = 10000;
	private final int server_port = 10003;
	private final int client_port = 10006;
	private final int sock_timeout = 2000;
	
	private ServerSocket serverSocket;
	private Socket socket;
	private DataInputStream dis;
	
	public Broker() {
		try {
			serverSocket = new ServerSocket(broker_port);
		} catch (IOException e) {
			System.out.println("소켓 초기화 실패");
		}
	}

	public void register_service(Message msg) {
		update_repository(msg);
		System.out.println(new Date() + "[Broker] 서버 등록 요청 승인 : " + msg.getAddr());
		msg.setFlag("ACK"); 
		response_request(msg, server_port);
	}

	private void update_repository(Message msg) {
		String addr = msg.getAddr();
		if (!serverProxyList.contains(addr)) {
			serverProxyList.add(addr);
		}
	}
	
	private void response_request(Message msg, int port) {
		Socket socket;
		DataOutputStream dos;

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(msg.getAddr(), port), sock_timeout);
			
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeUTF(msg.getType());
			dos.writeUTF(msg.getFlag());
			dos.writeUTF(msg.getAddr());
			dos.writeUTF(msg.getData());
			dos.close();
			socket.close();
		} catch (IOException e) {
			
		}
	}
	
	public void remove_service(Message msg) {
		String addr = msg.getData();
		if (serverProxyList.contains(addr)) {
			serverProxyList.remove(addr);
			System.out.println(new Date() + "[Broker] 서버 등록 해제 승인 : " + addr);
		}
	}

	private boolean find_server() {
		if(!serverProxyList.isEmpty()) {
			return true;
		}
		return false;
	}
	
	private void locate_server(Message msg) {
		if(find_server()) {
			serverProxyIndex++;
			serverProxyIndex %= serverProxyList.size();
			msg.setFlag("ACK");
			msg.setData(serverProxyList.get(serverProxyIndex));
			response_request(msg, client_port);
		} else {
			msg.setFlag("NAK");
			response_request(msg, client_port);
		}
	}

	public void run() {
		Thread.currentThread();
		while(!Thread.interrupted()) {
			try {
				socket = serverSocket.accept();
				dis = new DataInputStream(socket.getInputStream());
				
				String type = dis.readUTF();
				String flag = dis.readUTF();
				String addr = dis.readUTF(); addr = socket.getInetAddress().toString().replaceAll("/", "");
				String data = dis.readUTF();
				
				Message msg = new Message(type, flag, addr, data);

				switch(msg.getType()) {
					case "register_service":
						register_service(msg);
						break;
					case "remove_service":
						remove_service(msg);
						break;
					case "locate_server":
						locate_server(msg);
						break;
				}
				
				msg = null;
				dis.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Broker broker = new Broker();
		Thread t = new Thread(broker);
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e) {

		}
	}
}
