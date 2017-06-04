

public class Message {
	
	private String type; /* register, request */
	private String addr;
	private String data;
	
	public Message(String type, String addr, String data) {
		this.type = type;
		this.addr = addr;
		this.data = data;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
