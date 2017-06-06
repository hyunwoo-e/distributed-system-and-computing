package manager;

public class Timer extends Thread implements Timable {

	private String type;
	private Object manager;
	
	private float elapsed_time;
	private float expire_time;
	
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
					timeout(type);
					Thread.currentThread().interrupt();
				}

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void timeout(String type) {
		((Timable)manager).timeout(type);
	}
	
	public void startTimer(String type) {

	}
	
	public void stopTimer() {

	}
}
