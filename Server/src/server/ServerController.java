package server;

import java.io.IOException;

import election.ElectionManager;
import node.*;
import resource.NodeManager;
import resource.ResourceManager;
import timer.*;

public class ServerController {
	public ElectionManager electionManager;
	public NodeManager nodeManager;
	public ResourceManager resourceManager;
	
	public NameNode nameNode;
	public DataNode dataNode;
	private Thread nameNodeThread;
	private Thread dataNodeThread;
	
	public ServerController() {
		
	}
	
	public synchronized void start_managers() {
		if(Server.myAddr.equals(Server.getCoordinator()))
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
	
	public synchronized void stop_managers() {
			stop_resource_manager();
			stop_node_manager();
			stop_election_manager();
			stop_name_node();
			stop_data_node();
	}
	
	public void start_election_manager() {
		if(electionManager == null) {
			electionManager = new ElectionManager();
		}
	}
	
	public void stop_election_manager() {
		if(electionManager != null) {
			electionManager.destroy_manager();
			electionManager = null;
		}
	}
	
	public void start_resource_manager() {
		if(resourceManager == null) {
			Server.aliveServerMap.clear();
			resourceManager = new ResourceManager();
		}
	}
	
	public void stop_resource_manager() {
		if(resourceManager != null) {
			resourceManager.destroy_manager();
			resourceManager = null;
		}
	}
	
	public void start_node_manager() {
		if(nodeManager == null) {
			nodeManager = new NodeManager();
		}
	}
	
	public void stop_node_manager() {
		if(nodeManager != null) {
			nodeManager.destroy_manager();
			nodeManager = null;
		}
	}
	
	public void start_name_node() {
		if(nameNode == null) {
			nameNode = new NameNode();
			nameNodeThread = new Thread(nameNode);
			nameNodeThread.start();
		}
	}
	
	public void stop_name_node() {
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
	
	public void start_data_node() {
		if(dataNode == null) {
			dataNode = new DataNode();
			dataNodeThread = new Thread(dataNode);
			dataNodeThread.start();
		}
	}
	
	public void stop_data_node() {
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
}
