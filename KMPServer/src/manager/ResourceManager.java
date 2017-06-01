package manager;

import queue.*;
import server.*;
import java.util.*;

public class ResourceManager extends Thread implements MessageDefination {
	private Timer timer;
	
	public ResourceManager() {
		Common.nodes = new HashMap<String, Integer>();
		Common.nodeCount = 0;
		Thread.currentThread();
	}
	
	public synchronized void update_node() {
		/* ConcurrentModification 해결을 위해 복사 */
		HashMap<String, Integer> temp = (HashMap<String, Integer>)Common.nodes.clone();
		
		for(Map.Entry<String, Integer> entry : temp.entrySet()) {
			entry.setValue(entry.getValue() + TIMER_TICK);
			//System.out.println(entry.getKey() + " " +entry.getValue());
			if(entry.getValue() > HEART_BEATING_TIMEOUT) {
				temp.remove(entry.getKey());
				System.out.println(entry.getKey() + " Down");
			}
		}
		
		Common.nodes = temp;
		Common.nodeCount = Common.nodes.size();
	}
	
	public synchronized void respond_heartbeating(String ip) {
		System.out.println("respond_heartbeating() " + ip);
		Common.nodes.put(ip, 0);
		Common.nodeCount = Common.nodes.size();
	}
	
	public void run() {		
		while(!Thread.interrupted()) {
			Message msg = HeartBeatingMessageQueue.dequeue();
			if(msg != null) {
				respond_heartbeating(msg.getSender());
			}
			else {
				try {
					update_node();
					sleep(TIMER_TICK);
				} catch (InterruptedException e) {
					
				}
			}
		}
		
		if((timer != null) && timer.isAlive()) timer.interrupt();
		System.out.println("리소스 매니저 정상 종료");
	}
	
}
