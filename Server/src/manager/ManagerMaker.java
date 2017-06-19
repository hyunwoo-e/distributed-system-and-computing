package manager;

import server.*;

public class ManagerMaker {
	private int port;
	
	public Manager manager;
	private Thread managerThread;
	
	private Receiver receiver;
	private Thread receiverThread;
	
	private Sender sender;
	private Thread senderThread;

	public ManagerMaker(int port, String type) {
		this.port = port;

		switch(type) {
			case "ELECTIONMANAGER":
				make_send_queue();
				make_election_manager();
				make_receive_queue();
				
				senderThread.start();
				managerThread.start();
				receiverThread.start();
				break;
			case "NODEMANAGER":
				make_send_queue();
				make_node_manager();
				
				senderThread.start();
				managerThread.start();
				break;
			case "RESOURCEMANAGER":
				make_resource_manager();
				make_receive_queue();
				
				managerThread.start();
				receiverThread.start();
				break;
		}
	}
	
	public void make_send_queue() {
		sender = new Sender(port);
		senderThread = new Thread(sender);
	}
	
	public void make_receive_queue() {
		receiver = new Receiver(manager, port);
		receiverThread = new Thread(receiver);
	}
	
	public void make_election_manager() {
		manager = new ElectionManager(sender);
		managerThread = new Thread((Runnable)manager);
	}
	
	public void make_node_manager() {
		manager = new NodeManager(sender);
		managerThread = new Thread((Runnable)manager);
	}
	
	public void make_resource_manager() {
		manager = new ResourceManager();
		managerThread = new Thread((Runnable)manager);
	}
	
	public void destroy_manager() {
		try {
			if(senderThread != null) {
				sender.stop();
				senderThread.join();
			}
	
			if(managerThread != null) {
				((Manager)manager).stop();
				managerThread.join();
			}
				
			if(receiverThread != null) {
				receiver.stop();
				receiverThread.join();
			}
		} catch (InterruptedException e) {
			
		}
		
		sender = null;
		manager = null;
		receiver = null;
		
		senderThread = null;
		manager = null;
		receiverThread = null;
	}
}
