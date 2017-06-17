package resource;

import java.util.*;

import election.ElectionManager;
import server.*;
import timer.Timable;
import timer.Timer;

public class ResourceController {

	private final int port = 10002;

	private ResourceManager resourceManager;
	private Thread resourceManagerThread;
	
	private ReceiveQueue receiveQueue;
	private Thread receiveQueueThread;
	
	public ResourceController() {
		resourceManager = new ResourceManager();
		resourceManagerThread = new Thread(resourceManager);

		receiveQueue = new ReceiveQueue(resourceManager, port);
		receiveQueueThread = new Thread(receiveQueue);
		
		receiveQueueThread.start();
		resourceManagerThread.start();
	}
	
	public void destroy_manager() {
		resourceManager.stop();
		receiveQueue.stop();
		
		try {
			resourceManagerThread.join();
			receiveQueueThread.join();
		} catch (InterruptedException e) {
			
		}
		
		receiveQueue = null;
		receiveQueue = null;
		
		resourceManagerThread = null;
		receiveQueueThread = null;
	}
}