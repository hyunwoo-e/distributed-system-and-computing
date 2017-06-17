package resource;

import java.util.*;
import server.*;
import timer.Timable;
import timer.Timer;

public class ResourceManager extends PassiveQueue<Message> implements Runnable, Timable {
	private Timer timer;
	private boolean shouldStop;
	
	public ResourceManager() {
		
	}
	
	public synchronized void update_nodes() {
		/* ConcurrentModification 해결을 위해 복사 */
		HashMap<String, Integer> temp = (HashMap<String, Integer>)ServerInfo.getAliveServerMap().clone();
		
		for(Map.Entry<String, Integer> entry : ServerInfo.getAliveServerMap().entrySet()) {
			temp.put(entry.getKey(),entry.getValue() + HEARTBEAT_TICK);
			if(temp.get(entry.getKey()) > HEARTBEAT_TIMEOUT) {
				temp.remove(entry.getKey());
				System.out.println(entry.getKey() + " Down");
			}
		}
		
		ServerInfo.setAliveServerMap(temp);
	}
	
	public void timeout(String type) {
		Message msg = new Message("RESOURCEMANAGER", "TIMEOUT", "", "");
		super.accept(msg);
	}
	
	public synchronized void update_nodes(String ip) {
		ServerInfo.setAliveServerMap(ip);
	}
	
	public void startTimer(String type) {
		stopTimer();
		timer = new Timer(this, type);
		timer.start();
	}
	
	public void stopTimer() {
		if(timer != null)
		{
			timer.interrupt();
			try {
				timer.join();
			} catch (InterruptedException e) {
				
			}
			timer = null;
		}
	}
	
	public void stop() {
		stopTimer();
		shouldStop = true;
		destroy();
	}
	
	public void run() {	
		System.out.println("RESOURCEMANAGER UP");
		
		startTimer("HEARTBEAT");
		
		while(!shouldStop) {
			Message msg = super.release();
			if(msg != null) {
				switch(msg.getFlag()) {
				case "HEARTBEAT":
					update_nodes(msg.getAddr());
					break;
				case "TIMEOUT":
					update_nodes();
					startTimer("HEARTBEAT");
					break;
				}
			}
		}
		
		stopTimer();
		System.out.println("RESOURCE MANAGER DOWN");
	}	
}