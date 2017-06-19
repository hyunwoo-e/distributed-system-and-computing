package server;

import java.io.*;
import java.net.*;
import java.util.*;
import node.*;

public class ServerInfo {
	
	public static final int election_port = 10001;
	public static final int resource_port = 10002;
	
	public static ArrayList<String> totalServerList;
	public static HashMap<String, Integer> aliveServerMap;
	public static String myAddr;
	public static int myIndex;
	
	public static String coordinator;
	public static boolean isCoordinatorAlive;

	public ServerInfo() {
		init_ip();
		load_list();
		init_index();
	}

	public void init_ip() {
		try {
			myAddr = InetAddress.getLocalHost().getHostAddress().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/* Server Farm 내의 각 서버에 대한 주소를 list.txt 파일에서 읽어 초기화 */
	public void load_list() {
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
			
		}
	}
	
	public void init_index() {
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
	}
	
	public static synchronized void setIsCoordinatorAlive(boolean isAlive) {
		isCoordinatorAlive = isAlive;
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
}