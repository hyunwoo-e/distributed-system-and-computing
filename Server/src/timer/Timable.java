package timer;

public interface Timable {
	
	public final int HEARTBEAT_TICK = 5000;
	public final int HEARTBEAT_TIMEOUT = 50000;
	
	public void startTimer(String type);
	public void stopTimer();
	public void timeout(String type);
}
