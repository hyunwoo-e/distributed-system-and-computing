package server;

import java.io.*;
import java.net.*;
import java.util.*;
import manager.*;

public class Server extends Thread {
	private final String ListFile = "list.txt";
	
	private Sender sender;
	private Receiver receiver;
	
	public Server() {
		Thread.currentThread();
	}
	
	/* 서버 초기화 */
	public void init() {
		initIp();
		loadList();
		initIndex();
	}
	
	public void initIndex() {
		for(int i = 0 ; i < Common.list.size(); i++) {
			if(Common.ip.equals(Common.list.get(i).toString())) {
				Common.index = i;
			}
		}
	}
		
	public void initIp() {
		try {
			Common.ip = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/* Server Farm 내의 각 서버에 대한 주소를 list.txt 파일에서 읽어 초기화 */
	public void loadList() {
		Common.list = new ArrayList<String>();
		String str ="";
		
		/* ipAddress 리스트 생성 */
		try {
			BufferedReader br = new BufferedReader(new FileReader(ListFile));			
			while((str = br.readLine()) != null) {
				Common.list.add(str);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		init();
		
		if(Common.index == -1) {
			System.out.println("로컬 서버를 목록에서 찾을 수 없음");
			return;
		}
		
		/* 송신쓰레드, 수신쓰레드를 생성 */
		sender = new Sender();
		receiver = new Receiver();
		
		sender.start();
		receiver.start();
		
		Common.em = new ElectionManager();
		Common.em.start();
		
		while(!Thread.interrupted()) {
			/* Coordinator는 Up, Slave만 Down -> NameNode는 이를 감안하고 DataNode에 작업 분배 및 종합할 수 있어야 함. */
			
			if(Common.coordinator.equals(Common.ip)) {
				/* 자신이 Coordinator일 경우 ResourceManager를 실행 */
				if(Common.rm == null) {
					Common.rm = new ResourceManager();
					Common.rm.start();
				}
			}
			else {
				/* 자신이 Coordinator가 아닐 경우 ResourceManager를 종료 */
				if(Common.rm != null) {
					Common.nodes.clear();
					Common.nodeCount = 0;
					Common.rm.interrupt();
					Common.rm = null;
				}
			}
			
			if(Common.isAlive == true) {
				/* Coordinator가 정상 동작 중 이라면 NodeManager 실행 */
				if(Common.nm == null) {
					Common.nm = new NodeManager();
					Common.nm.start();
				}
			}
		}
		
		System.out.println("서버 정상 종료");
	}
	
	public static void main (String[] args) {
		Server server = new Server();
		server.start();	
		try {
			server.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}