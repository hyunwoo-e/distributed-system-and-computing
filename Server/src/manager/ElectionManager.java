package manager;

import server.Message;
import server.PassiveQueue;
import server.Server;

public class ElectionManager extends PassiveQueue<Message> implements Runnable, Timable {
	private Timer timer;
	private boolean shouldStop;

	public ElectionManager() {
		shouldStop = false;
	}
	
	/* Election �޽����� ������ ������ ������ �ڽ��� Coordinator�� ��
	 * Ok �޽����� �ް� Coordinator �޽����� ���� �ð� ���� ���� ���ϸ� Election�� �ٽ� ���� */
	public void timeout(String type) {
		Message msg;
		switch (type) {
			case "ELECTION" :
				System.out.println("TIMEOUT(ELECTION)");
				msg = new Message("ELECTION", "TIMEOUT", "", "ELECTION");
				super.accept(msg);
				break;
			case "OK" :
				System.out.println("TIMEOUT(OK)");
				msg = new Message("ELECTION", "TIMEOUT", "", "OK");
				super.accept(msg);
				break;
		}
	}
	
	public void start_election() {
		Server.setIsElectionStarted(true);		
		Message msg = new Message("ELECTION", "START", "", "");
		super.accept(msg);
	}
	
	/* �ڽ��� index���� ���� ������ Coordinator �޽����� ���� */
	public void send_coordinator() {
		for(int i = 0 ; i <= Server.getMyIndex(); i++) {
			Message smsg = new Message("ELECTION", "COORDINATOR", Server.getTotalServerList().get(i), Server.getMyAddr());
			Server.mQ.accept(smsg);
		}
	}
	
	/* Coordinator �޽����� ������  �ش� ������  Coordinator�� ���� */ 
	public void respond_coordinator(Message rmsg) {
		Server.setCoordinator(rmsg.getData());
		stopTimer();
	}
	
	/* Ok �޽����� ������ Coordinator�� �����ϰ� Coordinator �޽����� ��� */
	public void respond_ok() {
		startTimer("OK");
	}
	
	/* Election �޽����� ������ Ok �޽����� �����ϰ�, �ڽ��� index���� ū ������ Election �޽����� ���� */
	public void send_ok(Message rmsg) {		
		Message smsg = new Message("ELECTION", "OK", rmsg.getAddr(), "");
		Server.mQ.accept(smsg);
		send_election();
	}
	
	public void respond_election(Message rmsg){
		Server.setIsElectionStarted(true);
		send_ok(rmsg);
	}
	
	/* �ڽ��� index���� ū ������ Election �޽����� ���� */
	public void send_election() {
		for(int i = Server.getMyIndex() + 1 ; i < Server.getTotalServerList().size(); i++) {
			Message smsg = new Message("ELECTION", "ELECTION", Server.getTotalServerList().get(i), "");
			Server.mQ.accept(smsg);
		}
		
		startTimer("ELECTION");
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
			timer = null;
		}
	}
	
	public void run() {
		
		/* ���� �� Election�� ��û */
		start_election();
		
		while(!shouldStop) {
			Message msg = super.release();
			switch(msg.getFlag()) {
				case "START":
					send_election();
					break;
				case "ELECTION" :
					System.out.println("RECEIVE ELECTION FROM " + msg.getAddr());
					send_ok(msg);
					System.out.println("SEND OK");
					break;
				case "OK" :
					System.out.println("RECEIVE OK FROM " + msg.getAddr());
					respond_ok();
					break;
				case "COORDINATOR" :
					System.out.println("RECEIVE COORDINATOR FROM " + msg.getAddr());
					respond_coordinator(msg);
					System.out.println("COORDINATOR : " + msg.getAddr());
					break;
				case "TIMEOUT":
					if(msg.getData().equals("ELECTION")) {
						send_coordinator();
					}
					else {
						start_election();
					}
					break;
				case "EXIT":
					stopTimer();
					shouldStop = true;
					break;
			}
		}
		
		stopTimer();
		System.out.println("ELECTION MANAGER DOWN");
	}
}
