package node;

import java.util.*;

import server.Message;
import server.PassiveQueue;

public class DataNode extends PassiveQueue<Message> implements Runnable {
	private boolean shouldStop;
	
	public DataNode() {
		
	}
	
	public void run() {	
		
		while(!shouldStop) {
			Message msg = super.release();
			switch(msg.getFlag()) {
				case "EXIT":
					shouldStop = true;
					break;
			}
		}
		
		System.out.println("DATA NODE IS DOWN");
	}	
}