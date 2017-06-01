package manager;

import queue.*;
import server.Common;

public class NodeManager extends Thread implements MessageDefination {
	
	public NodeManager() {
		Thread.currentThread();
	}
	
	public void send_heartbeating() {
		//System.out.println("send_heartbeating() " + coordinator);
		Message msg = new Message(Common.ip, Common.coordinator, TO_RESOURCE_MANAGER, 0);
		MessageQueue.enqueue(msg);
	}
	
	public void run() {
		while(!Thread.interrupted()) {
			try {
				send_heartbeating();
				Thread.sleep(TIMER_TICK * 5);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		System.out.println("노드 매니저 정상 종료");
	}
}
