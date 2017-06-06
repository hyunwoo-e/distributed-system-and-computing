package manager;

import java.util.*;
import server.*;

public class ResourceManager extends PassiveQueue<Message> implements Runnable, Timable {
	private Timer timer;
	private boolean shouldStop;
	
	public ResourceManager() {
		
	}
	
	public void timeout(String type) {
		Message msg = new Message("ELECTION", "TIMEOUT", "", "");
		super.accept(msg);
	}
	
	public synchronized void update_nodes() {
		/* ConcurrentModification 해결을 위해 복사 */
		HashMap<String, Integer> temp = (HashMap<String, Integer>)Server.getAliveServerMap().clone();
		
		for(Map.Entry<String, Integer> entry : temp.entrySet()) {
			entry.setValue(entry.getValue() + HEARTBEAT_TICK);
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
			try {
				timer.join();
			} catch (InterruptedException e) {
				
			}
			timer = null;
		}
	}

	public void run() {	
		System.out.println("RESOURCEMANAGER UP");
		
		startTimer("HEARTBEAT");
		
		while(!shouldStop) {
			Message msg = super.release();
			switch(msg.getFlag()) {
				case "HEARTBEAT":
					update_nodes(msg.getAddr());
					break;
				case "TIMEOUT":
					update_nodes();
					startTimer("HEARTBEAT");
					break;
				case "EXIT":
					stopTimer();
					shouldStop = true;
					break;
			}
		}
		
		stopTimer();
		System.out.println("RESOURCE MANAGER DOWN");
	}	
}