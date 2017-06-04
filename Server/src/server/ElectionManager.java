package server;

public class ElectionManager extends PassiveQueue<Message> implements Runnable, Timable {
	private Timer timer;

	public ElectionManager() {
			
	}
	
	/* Election 메시지를 보내고 응답이 없으면 자신이 Coordinator가 됨
	 * Ok 메시지를 받고 Coordinator 메시지를 일정 시간 내에 받지 못하면 Election을 다시 시작 */
	public void timeout(String type) {
		switch (type) {
			case "ELECTION" :
				System.out.println("TIMEOUT(ELECTION)");
				send_coordinator();
				break;
			case "OK" :
				System.out.println("TIMEOUT(OK)");
				start_election();
				break;
		}
	}
	
	public void start_election() {
		Server.setElectionIsStarted(true);
		
		Message msg = new Message("ELECTION", "START", "", "");
		super.accept(msg);
	}
	
	/* 자신의 index보다 작은 서버에 Coordinator 메시지를 전송 */
	public void send_coordinator() {
		for(int i = 0 ; i <= Server.getMyIndex(); i++) {
			Message smsg = new Message("ELECTION", "COORDINATOR", Server.getTotalServerList().get(i), Server.getMyAddr());
			Server.mQ.accept(smsg);
		}
	}
	
	/* Coordinator 메시지를 받으면  해당 서버를  Coordinator로 설정 */ 
	public void respond_coordinator(Message rmsg) {
		Server.setCoordinator(rmsg.getData());
		stopTimer();
	}
	
	/* Ok 메시지를 받으면 Coordinator를 포기하고 Coordinator 메시지를 대기 */
	public void respond_ok() {
		startTimer("OK");
	}
	
	/* Election 메시지를 받으면 Ok 메시지를 전송하고, 자신의 index보다 큰 서버에 Election 메시지를 전송 */
	public void send_ok(Message rmsg) {
		Server.setElectionIsStarted(true);
		
		Message smsg = new Message("ELECTION", "OK", rmsg.getAddr(), "");
		Server.mQ.accept(smsg);
		send_election();
	}
	
	/* 자신의 index보다 큰 서버에 Election 메시지를 전송 */
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
		Thread.currentThread();
		while(!Thread.interrupted()) {
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
			}
		}
		
		stopTimer();
		System.out.println("ELECTION MANAGER DOWN");
	}
}
