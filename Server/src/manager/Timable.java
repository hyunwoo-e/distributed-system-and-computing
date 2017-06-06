package manager;

public interface Timable {
	public void startTimer(String type);
	public void stopTimer();
	public void timeout(String type);
	
	public final int TIMER_TICK = 500;
	public final int HEARTBEAT_TICK = 5000;
	
	public final int ELECTION_TIMEOUT = 5000;
	public final int OK_TIMEOUT = 10000;
	public final int HEARTBEAT_TIMEOUT = 50000;
}
