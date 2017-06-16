package node;

import java.io.*;
import java.net.*;
import java.util.*;

import server.Server;

public class Service {
	private final int data_port = 10003;
	private final int sock_timeout = 2000;

	private int id;
	public String addr;
	private KMP kmp;
	public HashMap<Integer, KMP> kmpMap;
	
	public HashSet<Integer> result;
	public HashMap<Integer, Boolean> done;
	
	public Service(int id, String addr, KMP kmp) {
		this.id = id;
		this.addr = addr;
		this.kmp = kmp; 
		kmpMap = new HashMap<Integer, KMP>();
		result = new HashSet<Integer>();
		done = new HashMap<Integer, Boolean>();
	}
	
	public void allocate(int cur, String dataNode) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(dataNode, data_port), sock_timeout);
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			
			dos.writeInt(id);
			dos.writeUTF(addr);
			dos.writeInt(cur);
			dos.writeUTF(kmpMap.get(cur).text);
			dos.writeUTF(kmpMap.get(cur).pattern);
			
			dos.close();
			socket.close();
		} catch (IOException e) {
			
		}
	}
	
	
	public void devide(int taskCount) {
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
			
			done.put(cur, false);
			
			allocate(cur, entry.getKey());

			cur = cur+len+j;
			i++;
		}
	}
}
