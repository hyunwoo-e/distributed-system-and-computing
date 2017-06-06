package server;

import java.io.*;
import java.net.*;
import java.util.*;
import manager.*;
import node.*;

public class Server implements Runnable {
	private static ArrayList<String> totalServerList;
	private static String myAddr;
	private static int myIndex;
	
	private static String coordinator;
	private static boolean isCoordinatorAlive;
	private static boolean isElectionStarted;
	
	private static HashMap<String, Integer> aliveServerMap;
	private static int aliveServerCount;
	
	private static ElectionManager electionManager;
	private static NodeManager nodeManager;
	private static ResourceManager resourceManager;
	private static NameNode nameNode;
	private static DataNode dataNode;
	public static MessageQueue mQ;
	
	private static Thread electionManagerThread;
	private static Thread nodeManagerThread;
	private static Thread resourceManagerThread;
	private static Thread nameNodeThread;
	private static Thread dataNodeThread;
	private static Thread messageQueueThread;
	
	private final int port = 10001;	
	private ServerSocket serverSocket;
		
	public Server() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mQ = new MessageQueue(port);
		messageQueueThread = new Thread(mQ);
		messageQueueThread.start();
	}
	
	/* ���� �ʱ�ȭ */
	private void init() {
		init_ip();
		load_list();
		init_index();
	}
	
	/* ���� ������ ip�ּҸ� ���� */
	private void init_ip() {
		try {
			myAddr = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/* Server Farm ���� �� ������ ���� �ּҸ� list.txt ���Ͽ��� �о� �ʱ�ȭ */
	private void load_list() {
		aliveServerMap = new HashMap<String, Integer>();
		totalServerList = new ArrayList<String>();
		String addr ="";
		
		/* ipAddress ����Ʈ ���� */
		try {
			BufferedReader br = new BufferedReader(new FileReader("list.txt"));			
			while((addr = br.readLine()) != null) {
				totalServerList.add(addr);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void init_index() {
		myIndex = -1;
		for(int i = 0 ; i < totalServerList.size(); i++) {
			if(myAddr.equals(totalServerList.get(i).toString())) {
				myIndex = i;
			}
		}
	}	

	public static ArrayList<String> getTotalServerList() {
		return totalServerList;
	}
	
	public static String getMyAddr() {
		return myAddr;
	}
	
	public static int getMyIndex() {
		return myIndex;
	}
	
	public static synchronized String getCoordinator() {
		return coordinator;
	}
	
	public static synchronized void setCoordinator(String c) {
		coordinator = c;
		setIsCoordinatorAlive(true);
		if(myAddr.equals(getCoordinator()))
		{
			/* �ڽ��� Coordinator�� ��� ResourceManager�� ���� */
			start_resource_manager();
			/* �ڽ��� Coordinator�� ��� NameNode�� ���� */
			start_name_node();
		}
		else {
			/* �ڽ��� Coordinator�� �ƴ� ��� ResourceManager�� ���� */
			stop_resource_manager();
			/* �ڽ��� Coordinator�� �ƴ� ��� NameNode�� ���� */
			stop_name_node();
		}
		/* Coordinator�� �����Ǹ� NodeManager�� ���� */
		start_node_manager();
		/* Coordinator�� �����Ǹ� DataNode�� ���� */
		start_data_node();
	}
	
	public static synchronized void setIsCoordinatorAlive(boolean isAlive) {
		isCoordinatorAlive = isAlive;

		/* Coordinator�� Up �Ǿ��� �� Election�� ����� */
		if(isCoordinatorAlive == true) {
			setIsElectionStarted(false);
		}

		/* Coordinator�� Down �Ǿ��� �� Election�� ���� */
		if(isCoordinatorAlive == false && isElectionStarted == false)
		{
			electionManager.start_election();
		}
	}
	
	public static synchronized boolean getIsCoordinatorAlive() {
		return isCoordinatorAlive;
	}
	
	public static void setIsElectionStarted(boolean isStarted) {
		isElectionStarted = isStarted;
	}
	
	public static boolean getIsElectionStarted() {
		return isElectionStarted;
	}
	
	public static HashMap<String, Integer> getAliveServerMap() {
		return aliveServerMap;
	}
	
	public static void setAliveServerMap(HashMap<String, Integer> temp) {
		/* HeartBeat ���� ���¸� ��� */
		for(Map.Entry<String, Integer> entry : aliveServerMap.entrySet()) {
			System.out.println("HEARTBEATING FROM " + entry.getKey() + " TTL " + (50000 - entry.getValue()) + "ms");
		}
		
		aliveServerMap = temp;
		aliveServerCount = aliveServerMap.size();
	}
	
	public static void setAliveServerMap(String ip) {
		aliveServerMap.put(ip, 0);
		aliveServerCount = aliveServerMap.size();
	}
	
	private static void start_election_manager() {
		if(electionManager == null) {
			electionManager = new ElectionManager();
			electionManagerThread = new Thread(electionManager);
			electionManagerThread.start();
			isElectionStarted = true;
		}
	}
	
	private static void stop_election_manager() {
		if(electionManager != null) {
			electionManager.accept(new Message("ELECTIONMANAGER","EXIT","",""));
			electionManagerThread = null;
			electionManager = null;
		}
	}
	
	private static void start_resource_manager() {
		if(resourceManager == null) {
			aliveServerMap.clear();
			aliveServerCount = 0;
			
			resourceManager = new ResourceManager();
			resourceManagerThread = new Thread(resourceManager);
			resourceManagerThread.start();
		}
	}
	
	private static void stop_resource_manager() {
		if(resourceManager != null) {
			resourceManager.accept(new Message("RESOURCEMANAGER","EXIT","",""));
			resourceManagerThread = null;
			resourceManager = null;
		}
	}
	
	private static void start_node_manager() {
		if(nodeManager == null) {
			nodeManager = new NodeManager();
			nodeManagerThread = new Thread(nodeManager);
			nodeManagerThread.start();
		}
	}
	
	private static void stop_node_manager() {
		if(nodeManager != null) {
			nodeManagerThread.interrupt();
			nodeManagerThread = null;
			nodeManager = null;
		}
	}
	
	private static void start_name_node() {
		if(nameNode == null) {
			nameNode = new NameNode();
			nameNodeThread = new Thread(nameNode);
			nameNodeThread.start();
		}
	}
	
	private static void stop_name_node() {
		if(nameNode != null) {
			nameNode.accept(new Message("NameNode","EXIT","",""));
			nameNodeThread = null;
			nameNode = null;
		}
	}
	
	private static void start_data_node() {
		if(dataNode == null) {
			dataNode = new DataNode();
			dataNodeThread = new Thread(dataNode);
			dataNodeThread.start();
		}
	}
	
	private static void stop_data_node() {
		if(dataNode != null) {
			dataNode.accept(new Message("DataNode","EXIT","",""));
			dataNodeThread = null;
			dataNode = null;
		}
	}
	
	
	public void run() {
		init();
		
		if(myIndex == -1) {
			System.out.println("���� ������ ��Ͽ��� ã�� �� ����");
			return;
		}
		
		start_election_manager();
		
		Thread.currentThread();
		while(!Thread.interrupted()) {			
			try {
				Socket socket = serverSocket.accept();
				DataInputStream dis = new DataInputStream(socket.getInputStream());

				String type = dis.readUTF();
				String flag = dis.readUTF();
				String addr = dis.readUTF(); addr = socket.getInetAddress().toString().replaceAll("/", "");
				String data = dis.readUTF();
				
				Message msg = new Message(type, flag, addr, data);
				
				switch (msg.getType()) {
					case "ELECTIONMANAGER":
						if(electionManager != null)
							electionManager.accept(msg);
						break;
					case "RESOURCEMANAGER" :
						if(resourceManager != null)
							resourceManager.accept(msg);
						break;
					case "NAMENODE" :
						if(nameNode != null)
							nameNode.accept(msg);
						break;
					case "DATANODE" :
						if(dataNode != null)
							dataNode.accept(msg);
						break;
				}
				
				dis.close();
				socket.close();
			} catch (IOException e) {
				
			}
		}

		stop_node_manager();
		stop_resource_manager();
		stop_election_manager();
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main (String[] args) {
		Server server = new Server();
		Thread t = new Thread(server);
		t.start();	
		try {
			t.join();
			System.out.println("SERVER IS DOWN");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}