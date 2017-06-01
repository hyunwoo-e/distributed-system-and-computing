package manager;

import queue.*;

public class Timer extends Thread implements MessageDefination {
	private int type;
	private float elapsed_time;
	private float expire_time;
	private Object Manager;
	
	public Timer(Object Manager, int type) {
		this.Manager = Manager;
		this.type = type;
		
		switch (type) {
			case ELECTION :
				expire_time = ELECTION_TIMEOUT;
				System.out.println("Timer(Election)");
				break;
			case OK :
				System.out.println("Timer(Ok)");
				expire_time = OK_TIMEOUT;
				break;
		}
		
		elapsed_time = 0.0f;
		
		Thread.currentThread();
	}
		
	public void electionTimer(int type) {
		elapsed_time += TIMER_TICK;

		if(elapsed_time >= expire_time) {
			Thread.currentThread().interrupt();	
			((ElectionManager)Manager).timeout(type);
		}
	}
	
	public void run() {
		
		while(!Thread.interrupted()) {
			try {
				Thread.sleep(TIMER_TICK);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
			switch (type) {
				case ELECTION : /* Coordinator 설정 */
					electionTimer(ELECTION);
					break;
				case OK : /* Election 재요청 */
					electionTimer(OK);
					break;
			}
		}
	}

}
