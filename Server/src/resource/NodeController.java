package resource;

import election.ElectionManager;
import server.*;
import timer.Timable;
import timer.Timer;

public class NodeController {
	private final int port = 10002;
	
	private NodeManager nodeManager;
	private Thread nodeManagerThread;

	private SendQueue sendQueue;
	private Thread sendQueueThread;
	
	private boolean shouldStop;
	private Timer timer;

	public NodeController() {
		sendQueue = new SendQueue(port);
		sendQueueThread = new Thread(sendQueue);
		
		nodeManager = new NodeManager(sendQueue);
		nodeManagerThread = new Thread(nodeManager);
		
		sendQueueThread.start();
		nodeManagerThread.start();
	}
	
	public void destroy_manager() {
		sendQueue.stop();
		nodeManager.stop();
		
		try {
			sendQueueThread.join();
			nodeManagerThread.join();
		} catch (InterruptedException e) {
			
		}
		
		sendQueue = null;
		nodeManager = null;
		
		sendQueueThread = null;
		nodeManagerThread = null;
	}
}
