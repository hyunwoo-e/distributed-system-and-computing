package manager;

public interface Manager {
	public void stop();
	
	public void startTimer(String type);
	public void stopTimer();
	public void timeout(String type);
}
