package resource;

import server.*;
import timer.Timable;
import timer.Timer;

public class NodeManager extends PassiveQueue<Message> implements Runnable, Timable {
	private boolean shouldStop;
	private Timer timer;
	
	private SendQueue sendQueue;
	
	public NodeManager(SendQueue sendQueue) {
		this.sendQueue = sendQueue;
		shouldStop = false;
	}
	
	public void send_heartbeat() {
		Message smsg = new Message("RESOURCEMANAGER", "HEARTBEAT", ServerInfo.getCoordinator(), "");
		sendQueue.accept(smsg);
	}
	
	public void stop() {
		stopTimer();
		sendQueue.stop();
		shouldStop = true;
		destroy();
	}

	public void timeout(String type) {
		Message msg = new Message("NODEMANAGER", "TIMEOUT", "", "");
		super.accept(msg);
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
		System.out.println("NODEMANAGER UP");
		
		startTimer("HEARTBEAT");
		
		while(!shouldStop) {
			Message msg = super.release();
			if(msg != null) {
				switch(msg.getFlag()) {
				case "TIMEOUT":
					send_heartbeat();
					startTimer("HEARTBEAT");
					break;
				}
			}
		}

		System.out.println("NODEMANAGER DOWN");
	}
}
