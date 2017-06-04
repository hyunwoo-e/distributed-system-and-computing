package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server implements Runnable {
	private static ArrayList<String> totalServerList;
	private static String myAddr;
	private static int myIndex;
	
	private static String coordinator;
	private static boolean coordinatorIsAlive;
	private static boolean electionIsStarted;
	
	private static HashMap<String, Integer> aliveServerMap;
	private static int aliveServerCount;
	
	private static ElectionManager electionManager;
	private static NodeManager nodeManager;
	private static ResourceManager resourceManager;
	public static MessageQueue mQ;
	
	private static Thread electionManagerThread;
	private static Thread nodeManagerThread;
	private static Thread resourceManagerThread;
	private static Thread messageQueueThread;
	
	private final int port = 10001;	
	private ServerSocket serverSocket;
		
	public Server() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mQ = new MessageQueue();
		messageQueueThread = new Thread(mQ);
		messageQueueThread.start();
	}
	
	/* 서버 초기화 */
	private void init() {
		init_ip();
		load_list();
		init_index();
	}
	
	/* 로컬 서버의 ip주소를 저장 */
	private void init_ip() {
		try {
			myAddr = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/* Server Farm 내의 각 서버에 대한 주소를 list.txt 파일에서 읽어 초기화 */
	private void load_list() {
		aliveServerMap = new HashMap<String, Integer>();
		totalServerList = new ArrayList<String>();
		String addr ="";
		
		/* ipAddress 리스트 생성 */
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

	public static synchronized ArrayList<String> getTotalServerList() {
		return totalServerList;
	}
	
	public static synchronized String getMyAddr() {
		return myAddr;
	}
	
	public static synchronized int getMyIndex() {
		return myIndex;
	}
	
	public static synchronized String getCoordinator() {
		return coordinator;
	}
	
	public static synchronized void setCoordinator(String c) {
		coordinator = c;
		setCoordinatorIsAlive(true);
		if(myAddr.equals(getCoordinator()))
		{
			/* 자신이 Coordinator일 경우 ResourceManager를 실행 */
			start_resource_manager();
		}
		else {
			/* 자신이 Coordinator가 아닐 경우 ResourceManager를 종료 */
			stop_resource_manager();
		}
		start_node_manager();
	}
	
	public static synchronized void setCoordinatorIsAlive(boolean isAlive) {
		coordinatorIsAlive = isAlive;
		
		if(coordinatorIsAlive == true) {
			electionIsStarted = false;
		}

		if(coordinatorIsAlive == false && electionIsStarted == false)
		{
			electionManager.start_election();
		}
	}
	
	public static synchronized boolean getCoordinatorIsAlive() {
		return coordinatorIsAlive;
	}
	
	public static synchronized void setElectionIsStarted(boolean isStarted) {
		electionIsStarted = isStarted;
	}
	
	public static synchronized boolean getElectionIsStarted() {
		return electionIsStarted;
	}
	
	public static synchronized HashMap<String, Integer> getAliveServerMap() {
		return aliveServerMap;
	}
	
	public static synchronized void setAliveServerMap(HashMap<String, Integer> temp) {	
		for(Map.Entry<String, Integer> entry : aliveServerMap.entrySet()) {
			System.out.println(entry.getKey() + " " +entry.getValue());
		}
		
		aliveServerMap = temp;
		aliveServerCount = aliveServerMap.size();
	}
	
	public static synchronized void setAliveServerMap(String ip) {
		aliveServerMap.put(ip, 0);
		aliveServerCount = aliveServerMap.size();
	}
	
	private static synchronized void start_election_manager() {
		if(electionManager == null) {
			electionManager = new ElectionManager();
			electionManagerThread = new Thread(electionManager);
			electionManagerThread.start();
			
			/* 진입 시 Election을 요청 */
			electionManager.start_election();
			electionIsStarted = true;
		}
	}
	
	private static synchronized void stop_election_manager() {
		if(electionManager != null) {
			electionManagerThread.interrupt();
			/* notify를 2번 하기 위함 */
			electionManager.accept(new Message("","","",""));
			electionManager.accept(new Message("","","",""));
			electionManagerThread = null;
			electionManager = null;
		}
	}
	
	private static synchronized void start_resource_manager() {
		if(resourceManager == null) {
			aliveServerMap.clear();
			aliveServerCount = 0;
			
			resourceManager = new ResourceManager();
			resourceManagerThread = new Thread(resourceManager);
			resourceManagerThread.start();
		}
	}
	
	private static synchronized void stop_resource_manager() {
		if(resourceManager != null) {
			resourceManagerThread.interrupt();
			/* notify를 2번 하기 위함 */
			resourceManager.accept(new Message("","","",""));
			resourceManager.accept(new Message("","","",""));
			resourceManagerThread = null;
			resourceManager = null;
		}
	}
	
	private static synchronized void start_node_manager() {
		if(nodeManager == null) {
			nodeManager = new NodeManager();
			nodeManagerThread = new Thread(nodeManager);
			nodeManagerThread.start();
		}
	}
	
	private static synchronized void stop_node_manager() {
		if(nodeManager != null) {
			nodeManagerThread.interrupt();
			nodeManagerThread = null;
			nodeManager = null;
		}
	}
	
	public void run() {
		init();
		
		if(myIndex == -1) {
			System.out.println("로컬 서버를 목록에서 찾을 수 없음");
			return;
		}
		
		start_election_manager();
		
		Thread.currentThread();
		while(!Thread.interrupted()) {
			
			/* Coordinator는 Up, Slave만 Down
			 * NameNode는 이를 감안하고 DataNode에 
			 * 작업 분배 및 종합할 수 있는 로직이 필요함. */
			
			try {
				Socket socket = serverSocket.accept();
				DataInputStream dis = new DataInputStream(socket.getInputStream());

				String type = dis.readUTF();
				String flag = dis.readUTF();
				String addr = dis.readUTF(); addr = socket.getInetAddress().toString().replaceAll("/", "");
				String data = dis.readUTF();
				
				Message msg = new Message(type, flag, addr, data);
				
				switch (msg.getType()) {
					case "ELECTION":
						if(electionManager != null)
							electionManager.accept(msg);
						break;
					case "HEARTBEAT" :
						if(resourceManager != null)
							resourceManager.accept(msg);
						break;
				}
				
				dis.close();
				socket.close();
			} catch (IOException e) {
				
			}
		}
		
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