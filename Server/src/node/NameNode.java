package node;

import java.util.*;
import server.*;

public class NameNode extends PassiveQueue<Message> implements Runnable {
	private boolean shouldStop;
	
	public NameNode() {
		
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
		
		System.out.println("NAME NODE IS DOWN");
	}	
}