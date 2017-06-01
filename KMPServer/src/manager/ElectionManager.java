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
	
	/* Election 메시지를 보내고 응답이 없으면 자신이 Coordinator가 됨
	 * Ok 메시지를 받고 Coordinator 메시지를 일정 시간 내에 받지 못하면 Election을 다시 시작 */
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
	
	/* 자신의 index보다 작은 서버에 Coordinator 메시지를 전송 */
	public void send_coordinator() {
		System.out.println("send_coordinator()");

		for(int i = 0 ; i <= Common.index; i++) {
			Message msg = new Message(Common.ip, Common.list.get(i), TO_ELECTION_MANAGER, COORDINATOR);
			MessageQueue.enqueue(msg);
		}
	}
	
	/* Coordinator 메시지를 받으면  해당 서버를  Coordinator로 설정 */ 
	public void respond_coordinator(String coordinator) {
		System.out.println("respond_coordinator()");
		setCoordinator(coordinator);
		if((timer != null) && timer.isAlive()) timer.interrupt();
	}
	
	/* Ok 메시지를 받으면 Coordinator를 포기하고 Coordinator 메시지를 대기 */
	public void respond_ok() {
		System.out.println("respond_ok()");
		if((timer != null) && timer.isAlive()) timer.interrupt();
		timer = new Timer(this, OK);
		timer.start();
	}
	
	/* Election 메시지를 받으면 Ok 메시지를 전송하고, 자신의 index보다 큰 서버에 Election 메시지를 전송 */
	public void respond_election(String electionSender) {
		System.out.println("respond_election()");
		Message msg = new Message(Common.ip, electionSender, TO_ELECTION_MANAGER, OK);
		MessageQueue.enqueue(msg);
		send_election();
	}
	
	/* 자신의 index보다 큰 서버에 Election 메시지를 전송 */
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
		System.out.println("일렉션 매니저 정상 종료");
	}
}
