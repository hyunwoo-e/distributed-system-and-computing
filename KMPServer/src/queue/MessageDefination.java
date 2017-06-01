package queue;

public interface MessageDefination {
	/* Manager 구분 */
	public final int TO_ELECTION_MANAGER = 1;
	public final int TO_RESOURCE_MANAGER = 2;
	
	/* ElectionManager 메시지 구분 */
	public final int ELECTION = 11;
	public final int OK = 12;
	public final int COORDINATOR = 13;
		
	public final int TIMER_TICK = 500;
	
	/* Socket 연결 Timeout */
	public final int SOCKET_TIMEOUT = 2000;
	
	/* Election Timeout */
	public final int ELECTION_TIMEOUT = 5000;
	public final int OK_TIMEOUT = 20000;
	
	/* HeartBeating Timeout*/
	public final int HEART_BEATING_TIMEOUT = 60000;
}
