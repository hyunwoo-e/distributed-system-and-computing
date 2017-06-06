package manager;

import server.Message;
import server.Server;

public class NodeManager implements Runnable, Timable {
	
	public NodeManager() {

	}
	
	public void send_heartbeat() {
		Message smsg = new Message("HEARTBEAT", "HEARTBEAT", Server.getCoordinator(), "");
		Server.mQ.accept(smsg);
	}
	
	public void run() {
		Thread.currentThread();
		while(!Thread.interrupted()) {
			try {
				Thread.sleep(TIMER_TICK * 5);
				send_heartbeat();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		System.out.println("NODE MANAGER DOWN");
	}

	public void timeout(String type) {
		
	}
	
	public void startTimer(String type) {

	}
	
	public void stopTimer() {

	}
}