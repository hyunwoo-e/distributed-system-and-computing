package broker;

public class Message {
	
	private String type;
	private String flag;
	private String addr; 
	private String data;
	
	public Message(String type, String flag, String addr, String data) {
		this.type = type;
		this.flag = flag;
		this.addr = addr;
		this.data = data;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
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
