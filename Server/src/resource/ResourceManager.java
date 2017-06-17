package resource;

import java.util.*;

import election.ElectionController;
import server.*;
import timer.Timable;
import timer.Timer;

public class ResourceManager {

	private final int port = 10002;

	private ResourceController resourceController;
	private Thread resourceControllerThread;
	
	private ReceiveQueue receiveQueue;
	private Thread receiveQueueThread;
	
	public ResourceManager() {
		resourceController = new ResourceController();
		resourceControllerThread = new Thread(resourceController);

		receiveQueue = new ReceiveQueue(resourceController, port);
		receiveQueueThread = new Thread(receiveQueue);
		
		receiveQueueThread.start();
		resourceControllerThread.start();
	}
	
	public void destroy_manager() {
		resourceController.stop();
		receiveQueue.stop();
		
		try {
			resourceControllerThread.join();
			receiveQueueThread.join();
		} catch (InterruptedException e) {
			
		}
		
		receiveQueue = null;
		receiveQueue = null;
		
		resourceControllerThread = null;
		receiveQueueThread = null;
	}
}