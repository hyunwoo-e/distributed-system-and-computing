package election;

import server.*;
import timer.Timable;
import timer.Timer;

public class ElectionManager {
	private final int port = 10001;
	
	private ElectionController electionController;
	private Thread electionControllerThread;
	
	private ReceiveQueue receiveQueue;
	private Thread receiveQueueThread;
	
	private SendQueue sendQueue;
	private Thread sendQueueThread;

	public ElectionManager() {
		sendQueue = new SendQueue(port);
		sendQueueThread = new Thread(sendQueue);
		
		electionController = new ElectionController(sendQueue);
		electionControllerThread = new Thread(electionController);

		receiveQueue = new ReceiveQueue(electionController, port);
		receiveQueueThread = new Thread(receiveQueue);
		
		sendQueueThread.start();
		receiveQueueThread.start();
		electionControllerThread.start();
	}
	
	public void destroy_manager() {
		sendQueue.stop();
		electionController.stop();
		receiveQueue.stop();
		
		try {
			sendQueueThread.join();
			electionControllerThread.join();
			receiveQueueThread.join();
		} catch (InterruptedException e) {
			
		}
		
		sendQueue = null;
		electionController = null;
		receiveQueue = null;
		
		sendQueueThread = null;
		electionControllerThread = null;
		receiveQueueThread = null;
	}
	
	public void restart_election() {
		electionController.start_election();
	}
}
