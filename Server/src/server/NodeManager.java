package server;

public class NodeManager extends PassiveQueue<Message> implements Runnable, Timable {
	
	public NodeManager() {

	}
	
	public void send_heartbeating() {
		Message smsg = new Message("HEARTBEAT", "", Server.getCoordinator(), "");
		Server.mQ.accept(smsg);
	}
	
	public void run() {
		Thread.currentThread();
		while(!Thread.interrupted()) {
			try {
				Thread.sleep(TIMER_TICK * 5);
				send_heartbeating();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
		System.out.println("NODE MANAGER DOWN");
	}

	public void timeout(String type) {
		
	}
}
