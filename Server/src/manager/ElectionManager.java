package manager;

import server.*;

public class ElectionManager  extends PassiveQueue<Message> implements Runnable, Manager {
	private boolean shouldStop;
	private Timer timer;
	private Sender sender;
	
	private boolean isElectionStarted;
	
	public ElectionManager(Sender sendQueue) {
		this.sender = sendQueue;
		shouldStop = false;
	}
	
	public void setIsElectionStarted(boolean isElectionStarted) {
		this.isElectionStarted = isElectionStarted;
	}
	
	public boolean getIsElectionStarted() {
		return isElectionStarted;
	}
	
	public void start_election() {		
		Message msg = new Message("ELECTIONMANAGER", "START", "", "");
		super.accept(msg);
	}
	
	/* �ڽ��� index���� ���� ������ Coordinator �޽����� ���� */
	public void send_coordinator() {
		if(getIsElectionStarted() == true) {
			for(int i = 0 ; i <= ServerInfo.myIndex; i++) {
				Message smsg = new Message("ELECTIONMANAGER", "COORDINATOR", ServerInfo.totalServerList.get(i), ServerInfo.myAddr);
				sender.accept(smsg);
				//Server.getMessageQueue().accept(smsg);
			}
		}
	}
	
	/* Coordinator �޽����� ������  �ش� ������  Coordinator�� ���� */ 
	public void respond_coordinator(Message rmsg) {
		setIsElectionStarted(false);
		ServerInfo.setCoordinator(rmsg.getData());
		Server.alertMessage.enqueue("START_MANAGER");
		stopTimer();
	}
	
	/* Ok �޽����� ������ Coordinator�� �����ϰ� Coordinator �޽����� ��� */
	public void respond_ok() {
		startTimer("OK");
	}
	
	/* Election �޽����� ������ Ok �޽����� �����ϰ�, �ڽ��� index���� ū ������ Election �޽����� ���� */
	public void send_ok(Message rmsg) {		
		Message smsg = new Message("ELECTIONMANAGER", "OK", rmsg.getAddr(), "");
		sender.accept(smsg);
		//Server.getMessageQueue().accept(smsg);
	}
	
	/* �ڽ��� index���� ū ������ Election �޽����� ���� */
	public void send_election() {
		if(getIsElectionStarted() == false) {
			setIsElectionStarted(true);
			for(int i = ServerInfo.myIndex + 1 ; i < ServerInfo.totalServerList.size(); i++) {
				Message smsg = new Message("ELECTIONMANAGER", "ELECTION", ServerInfo.totalServerList.get(i), "");
				sender.accept(smsg);
				//Server.getMessageQueue().accept(smsg);
			}
			startTimer("ELECTION");
		}
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
			try {
				timer.join();
			} catch (InterruptedException e) {
				
			}
			timer = null;
		}
	}
	
	public void stop() {
		stopTimer();
		shouldStop = true;
		destroy();
	}
	
	/* Election �޽����� ������ ������ ������ �ڽ��� Coordinator�� ��
	 * Ok �޽����� �ް� Coordinator �޽����� ���� �ð� ���� ���� ���ϸ� Election�� �ٽ� ���� */
	public void timeout(String type) {
		Message msg;
		switch (type) {
			case "ELECTION" :
				System.out.println("TIMEOUT(ELECTION)");
				msg = new Message("ELECTIONMANAGER", "TIMEOUT", "", "ELECTION");
				super.accept(msg);
				break;
			case "OK" :
				System.out.println("TIMEOUT(OK)");
				msg = new Message("ELECTIONMANAGER", "TIMEOUT", "", "OK");
				super.accept(msg);
				break;
		}
	}
	
	public void run() {
		System.out.println("ELECTIONMANAGER UP");

		/* ���� �� Election�� ��û */
		start_election();
		
		while(!shouldStop) {
			Message msg = super.release();
			if(msg != null) {
				switch(msg.getFlag()) {
					case "START":
						send_election();
						break;
					case "ELECTION" :
						System.out.println("RECEIVE ELECTION FROM " + msg.getAddr());
						send_ok(msg);
						System.out.println("SEND OK");
						send_election();
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
							setIsElectionStarted(false);
							start_election();
						}
						break;
				}
			}
			
		}
		
		System.out.println("ELECTION MANAGER DOWN");
	}
}
