package resource;

import election.ElectionController;
import server.*;
import timer.Timable;
import timer.Timer;

public class NodeManager {
	private final int port = 10002;
	
	private NodeController nodeController;
	private Thread nodeControllerThread;

	private SendQueue sendQueue;
	private Thread sendQueueThread;
	
	private boolean shouldStop;
	private Timer timer;

	public NodeManager() {
		sendQueue = new SendQueue(port);
		sendQueueThread = new Thread(sendQueue);
		
		nodeController = new NodeController(sendQueue);
		nodeControllerThread = new Thread(nodeController);
		
		sendQueueThread.start();
		nodeControllerThread.start();
	}
	
	public void destroy_manager() {
		sendQueue.stop();
		nodeController.stop();
		
		try {
			sendQueueThread.join();
			nodeControllerThread.join();
		} catch (InterruptedException e) {
			
		}
		
		sendQueue = null;
		nodeController = null;
		
		sendQueueThread = null;
		nodeControllerThread = null;
	}
}
