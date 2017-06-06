package node;

import java.util.*;
import server.*;

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
				case "":
					break;
			}
		}
		
		System.out.println("DATA NODE IS DOWN");
	}	
}