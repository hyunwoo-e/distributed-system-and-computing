package server;

import java.io.IOException;

import manager.*;
import node.*;
import proxy.*;

public class Server implements Runnable {
	public static GenericQueue<String> alertMessage;
	
	public static ManagerMaker electionManagerMaker;
	public static ManagerMaker nodeManagerMaker;
	public static ManagerMaker resourceManagerMaker;
	
	public static NameNode nameNode;
	public static DataNode dataNode;
	public static Proxy proxy;
	
	public Server() {
		alertMessage = new GenericQueue<String>();
	}
	
	public synchronized static void start_managers() {
		if(ServerInfo.myAddr.equals(ServerInfo.getCoordinator()))
		{
			/* 자신이 Coordinator일 경우 ResourceManager를 실행 */
			start_resource_manager();
			/* 자신이 Coordinator일 경우 NameNode를 실행 */
			start_name_node();
			/* 자신이 Coordinator일 경우 Proxy를 실행 */
			start_proxy();
		}
		else {
			/* 자신이 Coordinator가 아닐 경우 ResourceManager를 종료 */
			stop_resource_manager();
			/* 자신이 Coordinator가 아닐 경우 NameNode를 종료 */
			stop_name_node();
			/* 자신이 Coordinator가 아닐 경우 Proxy를 종료 */
			stop_proxy();
		}
		/* Coordinator가 선정되면 NodeManager를 실행 */
		stop_node_manager();
		start_node_manager();
		/* Coordinator가 선정되면 DataNode를 실행 */
		stop_data_node();
		start_data_node();
	}
	
	public synchronized static void stop_managers() {
			stop_resource_manager();
			stop_node_manager();
			stop_election_manager();
			
			stop_name_node();
			stop_data_node();
			
			stop_proxy();
	}
	
	public synchronized static  void start_election_manager() {
		if(electionManagerMaker == null) {
			electionManagerMaker = new ManagerMaker(ServerInfo.election_port, "ELECTIONMANAGER");
		}
	}
	
	public synchronized static  void stop_election_manager() {
		if(electionManagerMaker != null) {
			electionManagerMaker.destroy_manager();
			electionManagerMaker = null;
		}
	}
	
	public synchronized static  void start_resource_manager() {
		if(resourceManagerMaker == null) {
			ServerInfo.aliveServerMap.clear();
			resourceManagerMaker = new ManagerMaker(ServerInfo.resource_port, "RESOURCEMANAGER");
		}
	}
	
	public synchronized static  void stop_resource_manager() {
		if(resourceManagerMaker != null) {
			resourceManagerMaker.destroy_manager();
			resourceManagerMaker = null;
		}
	}
	
	public synchronized static  void start_node_manager() {
		if(nodeManagerMaker == null) {
			nodeManagerMaker = new ManagerMaker(ServerInfo.resource_port, "NODEMANAGER");
		}
	}
	
	public synchronized static  void stop_node_manager() {
		if(nodeManagerMaker != null) {
			nodeManagerMaker.destroy_manager();
			nodeManagerMaker = null;
		}
	}
	
	
	
	
	public synchronized static  void start_name_node() {
		if(nameNode == null) {
			nameNode = new NameNode();
			nameNode.start();
		}
	}
	
	public synchronized static  void stop_name_node() {
		if(nameNode != null) {
			nameNode._stop();
			try {
				nameNode.join();
			} catch (InterruptedException e) {
					
			}
			nameNode = null;
		}
	}
	
	public synchronized static  void start_data_node() {
		if(dataNode == null) {
			dataNode = new DataNode();
			dataNode.start();
		}
	}
	
	public synchronized static void stop_data_node() {
		if(dataNode != null) {
			dataNode._stop();
			try {
				dataNode.join();
			} catch (InterruptedException e) {
				
			}
			dataNode = null;
		}
	}
	
	
	
	
	
	public synchronized static  void start_proxy() {
		if(proxy == null) {
			proxy = new Proxy();
			proxy.start();
		}
	}
	
	public synchronized static void stop_proxy() {
		if(proxy != null) {
			proxy._stop();
			try {
				proxy.join();
			} catch (InterruptedException e) {
				
			}
			proxy = null;
		}
	}
	
	
	
	
	
	
	public void run() {
		if(ServerInfo.myIndex == -1) {
			System.out.println("로컬 서버를 목록에서 찾을 수 없음");
			return;
		}
		System.out.println("SERVER UP");

		start_election_manager();

		Thread.currentThread();
		while(!Thread.interrupted()) {
			resolveAlertMessage();
		}
	
		stop_managers();
	}
	
	public static synchronized void registerAlertMessage(String m) {
		alertMessage.enqueue(m);
	}
	
	public static synchronized void resolveAlertMessage() {
		if(alertMessage.size() > 0) {
			switch(alertMessage.dequeue()) {
			case "START_MANAGER":
				start_managers();
			break;
			case "START_ELECTION":
				if(ServerInfo.getIsCoordinatorAlive() == false)
				{
					((ElectionManager)electionManagerMaker.manager).start_election();
				}
			break;
			}
		}
	}

	public static void main (String[] args) {
		ServerInfo serverInfo = new ServerInfo();
		Server server = new Server();
		Thread t = new Thread(server);
		t.start();	
		try {
			t.join();
			System.out.println("SERVER DOWN");
		} catch (InterruptedException e) {
			
		}
	}
}
