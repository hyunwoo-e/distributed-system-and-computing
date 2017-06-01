package queue;

public class Message {
	private String sender;
	private String receiver;
	private int manager;
	private int type;
	
	public Message(String sender, String receiver, int manager, int type) {
		this.sender = sender;
		this.receiver = receiver;
		this.manager = manager;
		this.type = type;
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getReceiver() {
		return receiver;
	}	
	
	public int getManager() {
		return manager;
	}
	
	public int getType() {
		return type;
	}
}
