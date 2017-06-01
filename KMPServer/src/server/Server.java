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
	
	/* ���� �ʱ�ȭ */
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
	
	/* Server Farm ���� �� ������ ���� �ּҸ� list.txt ���Ͽ��� �о� �ʱ�ȭ */
	public void loadList() {
		Common.list = new ArrayList<String>();
		String str ="";
		
		/* ipAddress ����Ʈ ���� */
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
			System.out.println("���� ������ ��Ͽ��� ã�� �� ����");
			return;
		}
		
		/* �۽ž�����, ���ž����带 ���� */
		sender = new Sender();
		receiver = new Receiver();
		
		sender.start();
		receiver.start();
		
		Common.em = new ElectionManager();
		Common.em.start();
		
		while(!Thread.interrupted()) {
			/* Coordinator�� Up, Slave�� Down -> NameNode�� �̸� �����ϰ� DataNode�� �۾� �й� �� ������ �� �־�� ��. */
			
			if(Common.coordinator.equals(Common.ip)) {
				/* �ڽ��� Coordinator�� ��� ResourceManager�� ���� */
				if(Common.rm == null) {
					Common.rm = new ResourceManager();
					Common.rm.start();
				}
			}
			else {
				/* �ڽ��� Coordinator�� �ƴ� ��� ResourceManager�� ���� */
				if(Common.rm != null) {
					Common.nodes.clear();
					Common.nodeCount = 0;
					Common.rm.interrupt();
					Common.rm = null;
				}
			}
			
			if(Common.isAlive == true) {
				/* Coordinator�� ���� ���� �� �̶�� NodeManager ���� */
				if(Common.nm == null) {
					Common.nm = new NodeManager();
					Common.nm.start();
				}
			}
		}
		
		System.out.println("���� ���� ����");
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