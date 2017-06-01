package manager;

import queue.*;
import server.*;

public class ElectionManager extends Thread implements MessageDefination {
	private Timer timer;

	public ElectionManager() {
			Thread.currentThread();
	}
	
	public void setCoordinator(String coordinator) {
		Common.coordinator = coordinator;
		Common.isAlive = true;
	}
	
	/* Election �޽����� ������ ������ ������ �ڽ��� Coordinator�� ��
	 * Ok �޽����� �ް� Coordinator �޽����� ���� �ð� ���� ���� ���ϸ� Election�� �ٽ� ���� */
	public void timeout(int type) {
		switch (type) {
			case ELECTION :
				System.out.println("timeout(Election)");
				send_coordinator();
				break;
			case OK :
				System.out.println("timeout(Ok)");
				send_election();
				break;
		}
	}
	
	/* �ڽ��� index���� ���� ������ Coordinator �޽����� ���� */
	public void send_coordinator() {
		System.out.println("send_coordinator()");

		for(int i = 0 ; i <= Common.index; i++) {
			Message msg = new Message(Common.ip, Common.list.get(i), TO_ELECTION_MANAGER, COORDINATOR);
			MessageQueue.enqueue(msg);
		}
	}
	
	/* Coordinator �޽����� ������  �ش� ������  Coordinator�� ���� */ 
	public void respond_coordinator(String coordinator) {
		System.out.println("respond_coordinator()");
		setCoordinator(coordinator);
		if((timer != null) && timer.isAlive()) timer.interrupt();
	}
	
	/* Ok �޽����� ������ Coordinator�� �����ϰ� Coordinator �޽����� ��� */
	public void respond_ok() {
		System.out.println("respond_ok()");
		if((timer != null) && timer.isAlive()) timer.interrupt();
		timer = new Timer(this, OK);
		timer.start();
	}
	
	/* Election �޽����� ������ Ok �޽����� �����ϰ�, �ڽ��� index���� ū ������ Election �޽����� ���� */
	public void respond_election(String electionSender) {
		System.out.println("respond_election()");
		Message msg = new Message(Common.ip, electionSender, TO_ELECTION_MANAGER, OK);
		MessageQueue.enqueue(msg);
		send_election();
	}
	
	/* �ڽ��� index���� ū ������ Election �޽����� ���� */
	public void send_election() {
		System.out.println("send_election()");
		for(int i = Common.index + 1 ; i < Common.list.size(); i++) {
			Message msg = new Message(Common.ip, Common.list.get(i), TO_ELECTION_MANAGER, ELECTION);
			MessageQueue.enqueue(msg);
		}
		
		if((timer != null) && timer.isAlive()) timer.interrupt();
		timer = new Timer(this, ELECTION);
		timer.start();
	}
	
	public void run() {	
		send_election();
		
		while(!Thread.interrupted()) {
			Message msg = ElectionMessageQueue.dequeue();
			if(msg != null) {
				switch(msg.getType()) {
				case ELECTION :
					System.out.println(msg.getSender() + " send Election");
					respond_election(msg.getSender());
					break;
				case OK :
					System.out.println(msg.getSender() + " send Ok");
					respond_ok();
					break;
				case COORDINATOR :
					System.out.println(msg.getSender() + " send Coordinator");
					respond_coordinator(msg.getSender());
					System.out.println(msg.getSender() + " is Coordinator");
					break;
				}
			}
			
			
		}
		
		if((timer != null) && timer.isAlive()) timer.interrupt();
		System.out.println("�Ϸ��� �Ŵ��� ���� ����");
	}
}
