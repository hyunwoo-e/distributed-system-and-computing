package server;

import java.io.*;
import java.net.*;
import java.util.*;
import manager.*;
import node.*;
import proxy.Proxy;

public class Server implements Runnable {
	private static ArrayList<String> totalServerList;
	private static String myAddr;
	private static int myIndex;
	
	private static String coordinator;
	private static boolean isCoordinatorAlive;
	
	private static HashMap<String, Integer> aliveServerMap;
	
	private static ElectionManager electionManager;
	private static NodeManager nodeManager;
	private static ResourceManager resourceManager;
	private static NameNode nameNode;
	private static DataNode dataNode;
	private static ReceiveQueue receiveQueue;
	//private static Proxy proxy;
	
	private static Thread electionManagerThread;
	private static Thread nodeManagerThread;
	private static Thread resourceManagerThread;
	private static Thread nameNodeThread;
	private static Thread dataNodeThread;
	private static Thread receiveQueueThread;
	//private static Thread proxyThread;
	
		
	public Server() {

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
	
	public static PassiveQueue<Message> getManager(String type) {
		switch (type) {
		case "ELECTIONMANAGER":
			return electionManager;
		case "RESOURCEMANAGER" :
			return resourceManager;
		case "NODEMANAGER" :
			return nodeManager;	
		}
		return null;
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
		stop_node_manager();
		start_node_manager();
		/* Coordinator�� �����Ǹ� DataNode�� ���� */
		stop_data_node();
		start_data_node();
	}
	
	public static synchronized void setIsCoordinatorAlive(boolean isAlive) {
		isCoordinatorAlive = isAlive;
		
		/* Coordinator�� Down �Ǿ��� �� Election�� ���� */
		if(isCoordinatorAlive == false)
		{
			electionManager.start_election();
		}
	}
	
	public static synchronized boolean getIsCoordinatorAlive() {
		return isCoordinatorAlive;
	}
	
	public static HashMap<String, Integer> getAliveServerMap() {
		return aliveServerMap;
	}
	
	public static void setAliveServerMap(HashMap<String, Integer> temp) {
		aliveServerMap = temp;
	}
	
	public static void setAliveServerMap(String ip) {
		aliveServerMap.put(ip, 0);
		System.out.println("HEARTBEAT FROM :" + ip + " / THE NUMBER OF SERVER :" + aliveServerMap.size());
	}
	
	private static void start_election_manager() {
		if(electionManager == null) {
			electionManager = new ElectionManager();
			electionManagerThread = new Thread(electionManager);
			electionManagerThread.start();
		}
	}
	
	private static void stop_election_manager() {
		if(electionManager != null) {
			electionManager.accept(new Message("ELECTIONMANAGER","EXIT","",""));
			try {
				electionManagerThread.join();
			} catch (InterruptedException e) {
				
			}
			electionManagerThread = null;
			electionManager = null;
		}
	}
	
	private static void start_resource_manager() {
		if(resourceManager == null) {
			aliveServerMap.clear();
			
			resourceManager = new ResourceManager();
			resourceManagerThread = new Thread(resourceManager);
			resourceManagerThread.start();
		}
	}
	
	private static void stop_resource_manager() {
		if(resourceManager != null) {
			resourceManager.accept(new Message("RESOURCEMANAGER","EXIT","",""));
			try {
				resourceManagerThread.join();
			} catch (InterruptedException e) {
				
			}
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
			nodeManager.accept(new Message("NODEMANAGER","EXIT","",""));
			try {
				nodeManagerThread.join();
			} catch (InterruptedException e) {
				
			}
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
			try {
				if(nameNode.serverSocket != null) {
					nameNodeThread.interrupt();
					nameNode.serverSocket.close();
					try {
						nameNodeThread.join();
					} catch (InterruptedException e) {
						
					}
				}
			} catch (IOException e) {
				
			}
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
			try {
				if(dataNode.serverSocket != null) {
					dataNodeThread.interrupt();
					dataNode.serverSocket.close();
					try {
						dataNodeThread.join();
					} catch (InterruptedException e) {
						
					}
				}
			} catch (IOException e) {
				
			}
			dataNodeThread = null;
			dataNode = null;
		}
	}
	
	private static void start_receive_queue() {
		if(receiveQueue == null) {
			receiveQueue = new ReceiveQueue();		
			receiveQueueThread = new Thread(receiveQueue);
			receiveQueueThread.start();
		}
	}
	
	private static void stop_receive_queue() {
		if(receiveQueue != null) {
			try {
				if(receiveQueue.serverSocket != null) {
					receiveQueueThread.interrupt();
					receiveQueue.serverSocket.close();
					try {
						receiveQueueThread.join();
					} catch (InterruptedException e) {
						
					}
				}
			} catch (IOException e) {
				
			}
			receiveQueueThread = null;
			receiveQueue = null;
		}
	}

	/*
	private static void start_proxy() {
		if(proxy == null) {
			proxy = new Proxy();
			proxyThread = new Thread(proxy);
			proxyThread.start();
		}
	}
	
	private static void stop_proxy() {
		if(proxy != null) {
			try {
				proxyThread.interrupt();
				proxy.serverSocket.close();
				try {
					proxyThread.join();
				} catch (InterruptedException e) {
					
				}
			} catch (IOException e) {
				
			}
			proxyThread = null;
			proxy = null;
		}
	}
	*/
	
	public void run() {
		init();
		
		if(myIndex == -1) {
			System.out.println("���� ������ ��Ͽ��� ã�� �� ����");
			return;
		}
		System.out.println("SERVER UP");

		start_receive_queue();
		start_election_manager();

		Thread.currentThread();
		while(!Thread.interrupted());
	
		stop_receive_queue();
		stop_node_manager();
		stop_resource_manager();
		stop_election_manager();
	}
	
	public static void main (String[] args) {
		Server server = new Server();
		Thread t = new Thread(server);
		t.start();	
		try {
			t.join();
			System.out.println("SERVER DOWN");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}