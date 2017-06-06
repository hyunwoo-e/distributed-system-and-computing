package node;

import java.util.*;
import server.*;

public class NameNode extends PassiveQueue<Message> implements Runnable, ServerProxy {
	private boolean shouldStop;

	public NameNode() {
		
	}
	
	public void run() {	
		
		/* Coordinator�� Up, Slave�� Down �̸� ����ؼ� DataNode�� �۾� �й� �� ������ �� �ִ� ������ �ʿ���. */
		
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