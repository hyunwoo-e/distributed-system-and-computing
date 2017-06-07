package node;

import java.io.*;
import java.net.*;
import java.util.*;

import server.Server;

public class Service {
	private final int data_port = 10003;
	private final int sock_timeout = 2000;

	private KMP kmp;
	private HashMap<Integer, KMP> kmpMap;
	private String addr;
	
	public int taskCount;
	public HashSet<Integer> result;
	public HashSet<Integer> done;
	
	public Service(String addr, KMP kmp) {
		this.addr = addr;
		this.kmp = kmp; 
		kmpMap = new HashMap<Integer, KMP>();
		taskCount = Server.getAliveServerMap().size();
		result = new HashSet<Integer>();
		done = new HashSet<Integer>();
	}
	
	public void devide() {
		int i = 0;
		int cur = 0;
		int len = kmp.text.length() / taskCount;
		int mod = kmp.text.length() % taskCount;
		int j;
		
		for(Map.Entry<String, Integer> entry : Server.getAliveServerMap().entrySet()) {
			
			if(mod > i) j = 1; else j =0;
			
			if(i < taskCount-1)
				kmpMap.put(cur, new KMP(kmp.text.substring(cur, cur+len+j+kmp.pattern.length()), kmp.pattern));
			else
				kmpMap.put(cur, new KMP(kmp.text.substring(cur), kmp.pattern));
			
			try {
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(entry.getKey(), data_port), sock_timeout);
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				
				dos.writeUTF(addr);
				dos.writeInt(cur);
				dos.writeUTF(kmpMap.get(cur).text);
				dos.writeUTF(kmpMap.get(cur).pattern);
				
				dos.close();
				socket.close();
			} catch (IOException e) {
				/* Coordinator는 Up, Slave만 Down 이를 고려해서 DataNode에 작업 분배 및 종합할 수 있는 로직이 필요함. */
			}

			cur = cur+len+j;
			i++;
		}
	}
}
