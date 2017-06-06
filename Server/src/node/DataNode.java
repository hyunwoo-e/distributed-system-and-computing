package node;

import java.util.*;
import server.*;

public class DataNode extends PassiveQueue<Message> implements Runnable {
	private boolean shouldStop;
	
	public DataNode() {
		
	}
	
	public void run() {	
		System.out.println("DATANODE UP");
		
		while(!shouldStop) {
			Message msg = super.release();
			switch(msg.getFlag()) {
				case "EXIT":
					shouldStop = true;
					break;

			}
		}
		
		System.out.println("DATANODE DOWN");
	}	
}