package election;

import server.*;
import timer.Timable;
import timer.Timer;

public class ElectionController {
	private final int port = 10001;
	
	private ElectionManager electionManager;
	private Thread electionManagerThread;
	
	private ReceiveQueue receiveQueue;
	private Thread receiveQueueThread;
	
	private SendQueue sendQueue;
	private Thread sendQueueThread;

	public ElectionController() {
		sendQueue = new SendQueue(port);
		sendQueueThread = new Thread(sendQueue);
		
		electionManager = new ElectionManager(sendQueue);
		electionManagerThread = new Thread(electionManager);

		receiveQueue = new ReceiveQueue(electionManager, port);
		receiveQueueThread = new Thread(receiveQueue);
		
		sendQueueThread.start();
		receiveQueueThread.start();
		electionManagerThread.start();
	}
	
	public void destroy_manager() {
		sendQueue.stop();
		electionManager.stop();
		receiveQueue.stop();
		
		try {
			sendQueueThread.join();
			electionManagerThread.join();
			receiveQueueThread.join();
		} catch (InterruptedException e) {
			
		}
		
		sendQueue = null;
		electionManager = null;
		receiveQueue = null;
		
		sendQueueThread = null;
		electionManagerThread = null;
		receiveQueueThread = null;
	}
	
	public void restart_election() {
		electionManager.start_election();
	}
}
