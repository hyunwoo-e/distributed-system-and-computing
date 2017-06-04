package broker;

import java.io.*;
import java.net.*;

public class MessageQueue extends PassiveQueue<Message> implements Runnable {
	private final int port = 10001;
	
	public synchronized void acceptMessage(Message msg) {
		super.accept(msg);
	}
	
	public void run() {
		Socket socket;
		DataOutputStream dos;
		Message msg;
		
		for(;;){
			msg = super.release();
			try {
				socket = new Socket(msg.getAddr(), port);
				
				dos = new DataOutputStream(socket.getOutputStream());
				dos.writeUTF(msg.getType());
				dos.writeUTF(msg.getFlag());
				dos.writeUTF(msg.getAddr());
				dos.writeUTF(msg.getData());
				
				dos.close();
				socket.close();
			} catch (IOException e) {

			}
		}
	}
}
