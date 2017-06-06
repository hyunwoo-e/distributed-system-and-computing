package node;

import java.io.IOException;
import java.util.*;
import proxy.*;
import server.*;

public class NameNode extends PassiveQueue<Message> implements Runnable {
	private boolean shouldStop;
	private Proxy proxy;
	private Thread proxyThread;
	
	public NameNode() {
		
	}
	
	private void start_proxy() {
		if(proxy == null) {
			proxy = new Proxy();
			proxyThread = new Thread(proxy);
			proxyThread.start();
		}
	}
	
	private void stop_proxy() {
		if(proxy != null) {
			try {
				proxy.serverSocket.close();
			} catch (IOException e) {
				
			}
			proxyThread.interrupt();
			proxyThread = null;
			proxy = null;
		}
	}
	
	public void run() {
		System.out.println("NAMENODE UP");
		
		start_proxy();		
		/* Coordinator�� Up, Slave�� Down �̸� ����ؼ� DataNode�� �۾� �й� �� ������ �� �ִ� ������ �ʿ���. */
		
		while(!shouldStop) {
			Message msg = super.release();
			switch(msg.getFlag()) {
				case "EXIT":
					shouldStop = true;
					break;
				case "":
					break;
			}
		}
		
		stop_proxy();
		System.out.println("NAMENODE DOWN");
	}	
}