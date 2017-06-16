package manager;

public interface Timable {
	public void startTimer(String type);
	public void stopTimer();
	public void timeout(String type);
}
