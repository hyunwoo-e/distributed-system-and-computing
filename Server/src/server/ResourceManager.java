package server;

import java.util.*;

public class ResourceManager extends PassiveQueue<Message> implements Runnable, Timable {
	private Timer timer;
	
	public ResourceManager() {
		
	}
	
	public void timeout(String type) {
		update_nodes();
		if(Server.getCoordinator().equals(Server.getMyAddr())) {
			startTimer("HEARTBEAT");
			//refreshTimer();
		} else {
			stopTimer();
		}
	}
	
	public synchronized void update_nodes() {
		/* ConcurrentModification 해결을 위해 복사 */
		HashMap<String, Integer> temp = (HashMap<String, Integer>)Server.getAliveServerMap().clone();
		
		for(Map.Entry<String, Integer> entry : temp.entrySet()) {
			entry.setValue(entry.getValue() + TIMER_TICK);
			if(entry.getValue() > HEARTBEAT_TIMEOUT) {
				temp.remove(entry.getKey());
				System.out.println(entry.getKey() + " Down");
			}
		}
		Server.setAliveServerMap(temp);
	}
	
	public synchronized void update_nodes(String ip) {
		Server.setAliveServerMap(ip);
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
			timer = null;
		}
	}

	public void run() {	
		startTimer("HEARTBEAT");
		
		Thread.currentThread();
		while(!Thread.interrupted()) {
			Message msg = super.release();
			update_nodes(msg.getAddr());
		}
		
		stopTimer();
		System.out.println("RESOURCE MANAGER IS DOWN");
	}
	
}
