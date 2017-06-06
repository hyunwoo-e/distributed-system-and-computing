package node;

import java.util.*;
import server.*;

public class NameNode extends PassiveQueue<Message> implements Runnable, ServerProxy {
	private boolean shouldStop;

	public NameNode() {
		
	}
	
	public void run() {	
		
		/* Coordinator는 Up, Slave만 Down 이를 고려해서 DataNode에 작업 분배 및 종합할 수 있는 로직이 필요함. */
		
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
		
		System.out.println("NAME NODE IS DOWN");
	}	
}