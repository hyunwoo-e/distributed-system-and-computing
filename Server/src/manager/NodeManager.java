package manager;

import server.*;

public class NodeManager extends PassiveQueue<Message> implements Runnable, Timable {
	private SendQueue sendQueue;
	private Thread sendQueueThread;
	private boolean shouldStop;
	private Timer timer;
	
	public NodeManager() {
		sendQueue = new SendQueue();
		sendQueueThread = new Thread(sendQueue);
		sendQueueThread.start();
	}
	
	public void send_heartbeat() {
		Message smsg = new Message("RESOURCEMANAGER", "HEARTBEAT", Server.getCoordinator(), "");
		sendQueue.accept(smsg);
		//Server.getMessageQueue().accept(smsg);
	}
	
	public void run() {
		System.out.println("NODEMANAGER UP");
		
		startTimer("HEARTBEAT");
		
		while(!shouldStop) {
			Message msg = super.release();
			switch(msg.getFlag()) {
			case "EXIT":
				stopTimer();
				shouldStop = true;
				break;
			case "TIMEOUT":
				send_heartbeat();
				startTimer("HEARTBEAT");
				break;
			}
		}
		
		sendQueue.stop();
		sendQueue.notify();
		
		System.out.println("NODEMANAGER DOWN");
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
}
