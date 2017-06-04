package server;

public class Timer extends Thread implements Timable {

	private String type;
	private Object Manager;
	
	private float elapsed_time;
	private float expire_time;
	
	public Timer(Object Manager, String type) {
		this.Manager = Manager;
		this.type = type;
		
		switch (type) {
			case "ELECTION" :
				System.out.println("TIMER(ELECTION)");
				expire_time = ELECTION_TIMEOUT;
				break;
			case "OK" :
				System.out.println("TIMER(OK)");
				expire_time = OK_TIMEOUT;
				break;
			case "HEARTBEAT":
				expire_time = TIMER_TICK * 5;
				break;
		}
		
		elapsed_time = 0.0f;
	}

	public void run() {
		Thread.currentThread();
		while(!Thread.interrupted()) {
			try {
				Thread.sleep(TIMER_TICK);

				elapsed_time += TIMER_TICK;
				if(elapsed_time >= expire_time) {
					timeout(type);
				}

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void timeout(String type) {
		((Timable)Manager).timeout(type);
	}
}
