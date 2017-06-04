package broker;

import java.io.*;
import java.net.*;
import java.util.*;

public class Broker implements Runnable {
	private ArrayList<String> serverProxyList = new ArrayList<String>();
	private int serverProxyIndex;
	
	private final int port = 10001;	
	private ServerSocket serverSocket;
	private Socket socket;
	private DataInputStream dis;
	
	private MessageQueue mQ;

	public Broker() {
		mQ = new MessageQueue();
		Thread t  = new Thread(mQ);
		t.start();
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("소켓 초기화 실패");
		}
	}

	public void register_service(Message msg) {
		if (update_repository(msg)) {
			System.out.println(new Date() + "[Broker] 서버 등록 요청 승인 : " + msg.getAddr());
			msg.setData("ACK");
		} else {
			System.out.println(new Date() + "[Broker] 서버 등록 요청 실패 : " + msg.getAddr());
			msg.setData("NAK");
		}
		mQ.acceptMessage(msg);
	}

	private boolean update_repository(Message msg) {
		/* 이전 coordinator를 제거하는 로직 필요 */
		String addr = msg.getAddr();
		if (!serverProxyList.contains(addr)) {
			serverProxyList.add(addr);
			return true;
		}
		return false;
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
			msg.setData(serverProxyList.get(serverProxyIndex));
		} else {
			msg.setData("NAK");
		}
		mQ.acceptMessage(msg);
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
					case "register":
						register_service(msg);
						break;
					case "locate":
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
	}
}
