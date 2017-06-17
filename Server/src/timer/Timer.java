package timer;

public class Timer extends Thread {

	private String type;
	private Object manager;
	
	private float elapsed_time;
	private float expire_time;
	
	private final int TIMER_TICK = 500;
	private final int HEARTBEAT_TICK = 5000;
	
	private final int ELECTION_TIMEOUT = 5000;
	private final int OK_TIMEOUT = 10000;
	private final int HEARTBEAT_TIMEOUT = 50000;
	
	public Timer(Object manager, String type) {
		this.manager = manager;
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
				expire_time = HEARTBEAT_TICK;
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
					((Timable)manager).timeout(type);
					Thread.currentThread().interrupt();
				}

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}