package server;

import java.io.*;
import java.net.*;
import java.util.*;

import node.*;
import proxy.Proxy;
import timer.*;

public class Server implements Runnable {
	public static ArrayList<String> totalServerList;
	public static HashMap<String, Integer> aliveServerMap;
	public static String myAddr;
	public static int myIndex;
	
	public static String coordinator;
	public static boolean isCoordinatorAlive;
	
	private static ServerController serverController;
	
	public Server() {
		init_ip();
		load_list();
		init_index();
		serverController = new ServerController();
	}
	
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

	public static synchronized String getCoordinator() {
		return coordinator;
	}
	
	public static synchronized void setCoordinator(String c) {
		coordinator = c;
		setIsCoordinatorAlive(true);
		serverController.start_managers();
	}
	
	public static synchronized void setIsCoordinatorAlive(boolean isAlive) {
		isCoordinatorAlive = isAlive;
		
		/* Coordinator�� Down �Ǿ��� �� Election�� ���� */
		if(isCoordinatorAlive == false)
		{
			serverController.electionManager.restart_election();
		}
	}
	
	public static synchronized boolean getIsCoordinatorAlive() {
		return isCoordinatorAlive;
	}
	
	public static synchronized HashMap<String, Integer> getAliveServerMap() {
		return aliveServerMap;
	}
	
	public static synchronized void setAliveServerMap(HashMap<String, Integer> temp) {
		aliveServerMap = temp;
	}
	
	public static synchronized void setAliveServerMap(String ip) {
		aliveServerMap.put(ip, 0);
		System.out.println("HEARTBEAT FROM :" + ip + " / THE NUMBER OF SERVER :" + aliveServerMap.size());
	}
	
	public void run() {
		if(myIndex == -1) {
			System.out.println("���� ������ ��Ͽ��� ã�� �� ����");
			return;
		}
		System.out.println("SERVER UP");

		serverController.start_election_manager();

		Thread.currentThread();
		while(!Thread.interrupted());
	
		serverController.stop_managers();
	}
	
	
	public static void main (String[] args) {
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